package com.example.demo.servecies;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.UserCredentialsDto;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.ApiAccessRequest;
import com.example.demo.entity.QUser;
import com.example.demo.entity.Secteur;
import com.example.demo.entity.Structure;
import com.example.demo.entity.User;
import com.example.demo.repository.ApiAccessRequestRepository;
import com.example.demo.repository.SecteurRepository;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.UserRepository;
import com.querydsl.core.types.dsl.BooleanExpression;



@Service
public class UserService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SecteurRepository secteurRepository;
	
	@Autowired
	private StructureRepository structureRepository;
	
	@Autowired
	private ApiAccessRequestRepository accessRequestRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Legacy method for adding a user with just username, password, and role
	 */
	public User addUser(String username, String password, String role) {
		if (userRepository.findByUsername(username).isPresent()) {
			throw new DuplicateKeyException("Username already used");
		}
		User user = new User(username, passwordEncoder.encode(password), role);
		return userRepository.save(user);
	}
	
	/**
	 * Add a new user with secteur and structure information
	 */
	public User addUser(String username, String password, String role, Secteur secteur, Structure structure) {
		if (userRepository.findByUsername(username).isPresent()) {
			throw new DuplicateKeyException("Username already used");
		}
		User user = new User(username, passwordEncoder.encode(password), role);
		user.setSecteur(secteur);
		user.setStructure(structure);
		return userRepository.save(user);
	}
	
	/**
	 * Create a new user from a Data Transfer Object (DTO)
	 */
	public User createUser(UserDTO userDTO) {
		if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
			throw new DuplicateKeyException("Username already used");
		}
		
		User user = new User();
		user.setUsername(userDTO.getUsername());
		user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		user.setRole(userDTO.getRole());
		
		// Set secteur from DTO if provided
		if (userDTO.getSecteurId() != null && !userDTO.getSecteurId().isEmpty()) {
			secteurRepository.findById(userDTO.getSecteurId())
				.ifPresent(user::setSecteur);
		}
		
		// Set structure from DTO if provided
		if (userDTO.getStructureId() != null && !userDTO.getStructureId().isEmpty()) {
			structureRepository.findById(userDTO.getStructureId())
				.ifPresent(user::setStructure);
		}
		
		return userRepository.save(user);
	}

	public User getUserById(String id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));
	}

	public User updateUser(String id, User user) {
		if (!id.equals(user.getId()))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID do not match");
		try {
			return userRepository.findById(id).map(u -> {
				u.setUsername(user.getUsername());
				u.setRole(user.getRole());
				return userRepository.save(u);
			}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));
		} catch (DuplicateKeyException exception) {
			throw new DuplicateKeyException("Username already used");
		}
	}

	public void deleteUser(String id) {
		if (!userRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
		}
		userRepository.deleteById(id);
	}

	public Page<User> getAllUsers(Pageable page, String username, String role, String secteurId, String structureId) {
		logger.info("Getting users with filters - username: {}, role: {}, secteurId: {}, structureId: {}",
			username, role, secteurId, structureId);
		
		// If we have a secteurId or structureId filter, we need to use a special approach
		if ((secteurId != null && !secteurId.isEmpty()) || (structureId != null && !structureId.isEmpty())) {
			return getUsersByCustomQuery(page, username, role, secteurId, structureId);
		}
		
		// Standard approach for other filters
		QUser quser = new QUser("user");
		BooleanExpression finalPredicate = null;

		// Username filter
		BooleanExpression usernameExp = (username != null && !username.isEmpty()) 
				? quser.username.containsIgnoreCase(username)
				: null;

		// Role filter
		BooleanExpression roleExp = (role != null && !role.isEmpty()) 
				? quser.role.eq(role) 
				: null;

		// Structure filter
		BooleanExpression structureExp = null;
		if (structureId != null && !structureId.isEmpty()) {
			structureExp = quser.structure.id.eq(structureId);
		}

		// Combine all expressions with AND operator
		if (usernameExp != null) {
			finalPredicate = usernameExp;
		}
		
		if (roleExp != null) {
			finalPredicate = (finalPredicate != null) ? finalPredicate.and(roleExp) : roleExp;
		}
		
		if (structureExp != null) {
			finalPredicate = (finalPredicate != null) ? finalPredicate.and(structureExp) : structureExp;
		}

		// Apply predicate if any filters are set
		if (finalPredicate != null) {
			logger.info("Applying filter predicate: {}", finalPredicate);
			Page<User> results = userRepository.findAll(finalPredicate, page);
			logger.info("Found {} users matching the criteria", results.getTotalElements());
			return results;
		}

		// No filters, return all users
		Page<User> allUsers = userRepository.findAll(page);
		logger.info("Returning all users: {}", allUsers.getTotalElements());
		return allUsers;
	}
	
	/**
	 * Get users with custom MongoDB query for proper handling of secteur and structure ID filters
	 */
	private Page<User> getUsersByCustomQuery(Pageable page, String username, String role, 
			String secteurId, String structureId) {
		
		logger.info("Using custom MongoDB query for advanced filtering");
		
		// Build a criteria object for MongoDB
		org.springframework.data.mongodb.core.query.Criteria criteria = null;
		
		// Add secteur filter if provided
		if (secteurId != null && !secteurId.isEmpty()) {
			// Based on the document structure, secteur is stored directly as an ObjectId
			criteria = org.springframework.data.mongodb.core.query.Criteria.where("secteur")
				.is(new org.bson.types.ObjectId(secteurId));
			logger.info("Added secteur filter: {}", secteurId);
		}
		
		// Create structure criteria if provided
		if (structureId != null && !structureId.isEmpty()) {
			// Structure is also stored as a direct ObjectId
			org.springframework.data.mongodb.core.query.Criteria structureCriteria = 
				org.springframework.data.mongodb.core.query.Criteria.where("structure")
					.is(new org.bson.types.ObjectId(structureId));
			
			// If we already have secteur criteria, AND it with structure
			if (criteria != null) {
				criteria = criteria.and("structure").is(new org.bson.types.ObjectId(structureId));
			} else {
				// Otherwise, use structure criteria as the main criteria
				criteria = structureCriteria;
			}
			logger.info("Added structure filter: {}", structureId);
		}
		
		// If we don't have any criteria at this point, create a default one that matches all documents
		if (criteria == null) {
			criteria = new org.springframework.data.mongodb.core.query.Criteria();
		}
		
		// Add additional criteria if needed
		if (username != null && !username.isEmpty()) {
			criteria = criteria.and("username").regex(username, "i");
		}
		
		if (role != null && !role.isEmpty()) {
			criteria = criteria.and("role").is(role);
		}
		
		// Create the query
		org.springframework.data.mongodb.core.query.Query query = 
			org.springframework.data.mongodb.core.query.Query.query(criteria);
		
		// Apply pagination
		query.with(page);
		
		// Execute the query and count
		long total = mongoTemplate.count(query, User.class);
		List<User> users = mongoTemplate.find(query, User.class);
		
		logger.info("Custom query found {} users with secteurId: {}", total, secteurId);
		
		// If we found no users, debug log all users with their secteur and structure IDs
		if (total == 0) {
			// Debug the current query
			logger.info("Debug - MongoDB query: {}", query.toString());
			
			// Log all users in the database and their IDs
			logger.info("No users found with the current filters. Checking all users...");
			userRepository.findAll().forEach(user -> {
				String userSecteurId = user.getSecteur() != null ? user.getSecteur().getId() : "null";
				String userStructureId = user.getStructure() != null ? user.getStructure().getId() : "null";
				logger.info("User: {}, SecteurId: {}, StructureId: {}", 
					user.getUsername(), userSecteurId, userStructureId);
			});
			
			// Log one user's details to understand the structure
			User sampleUser = userRepository.findByUsername("user").orElse(null);
			if (sampleUser != null) {
				if (sampleUser.getSecteur() != null) {
					logger.info("Sample user '{}' secteur details - Class: {}, ID: {}, toString(): {}", 
						sampleUser.getUsername(),
						sampleUser.getSecteur().getClass().getName(),
						sampleUser.getSecteur().getId(),
						sampleUser.getSecteur().toString());
				}
				if (sampleUser.getStructure() != null) {
					logger.info("Sample user '{}' structure details - Class: {}, ID: {}, toString(): {}", 
						sampleUser.getUsername(),
						sampleUser.getStructure().getClass().getName(),
						sampleUser.getStructure().getId(),
						sampleUser.getStructure().toString());
				}
			}
		}
		
		// Create a page object with the results
		return new org.springframework.data.domain.PageImpl<>(users, page, total);
	}

	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
	}
	
	/**
	 * Find a user by username
	 * @param username the username to search for
	 * @return an Optional containing the user if found
	 */
	public java.util.Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}
	
	/**
	 * Find a user by ID
	 * @param id the ID of the user to find
	 * @return an Optional containing the user if found
	 */
	public java.util.Optional<User> findById(String id) {
		return userRepository.findById(id);
	}

	/**
	 * Get the currently authenticated user
	 */
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
		}
		
		String username = authentication.getName();
		return loadUserByUsername(username);
	}
	
	/**
	 * Create a consumer account based on an approved API access request
	 * If the user already exists with the given email, update their role to include consumer access
	 * 
	 * @param request The approved API access request
	 * @return UserCredentialsDto containing the user credentials
	 */
	public UserCredentialsDto createConsumerAccount(ApiAccessRequest request) {
		// Check if user already exists with this email
		String email = request.getEmail();
		String username = email; // Use email as username
		String temporaryPassword = null;
		
		User consumer = null;
		try {
			// Try to find existing user by username/email
			consumer = loadUserByUsername(username);
			
			// If user exists but doesn't have consumer role, add it
			if (!consumer.getRole().contains("consumer")) {
				consumer.setRole(consumer.getRole() + ",consumer");
				consumer = userRepository.save(consumer);
			}
		} catch (UsernameNotFoundException e) {
			// User doesn't exist, create new consumer account
			temporaryPassword = generateTemporaryPassword();
			consumer = new User(username, passwordEncoder.encode(temporaryPassword), "consumer");
			
			// Set secteur if available
			if (request.getSecteur() != null && !request.getSecteur().isEmpty()) {
				secteurRepository.findById(request.getSecteur())
					.ifPresentOrElse(
						consumer::setSecteur,
						() -> {
							throw new IllegalArgumentException("Invalid Secteur ID: " + request.getSecteur());
						}
					);
			}
			
			// Set structure if available
			if (request.getStructure() != null && !request.getStructure().isEmpty()) {
				structureRepository.findById(request.getStructure())
					.ifPresentOrElse(
						consumer::setStructure,
						() -> {
							throw new IllegalArgumentException("Invalid Structure ID: " + request.getStructure());
						}
					);
			}
			
			consumer = userRepository.save(consumer);
			
			// TODO: Send email with temporary password to user
			// This would typically involve an email service
		}
		
		// Update consumer ID in the request
		request.setConsumerId(consumer.getId());
		
		// Save the updated request
		accessRequestRepository.save(request);
		
		// Return user credentials
		return new UserCredentialsDto(email, username, temporaryPassword);
	}
	
	/**
	 * Generate a temporary password for new consumer accounts
	 * 
	 * @return A random temporary password
	 */
	private String generateTemporaryPassword() {
		// Generate a random string of 8 characters
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			int index = (int) (chars.length() * Math.random());
			sb.append(chars.charAt(index));
		}
		return sb.toString();
	}
}
