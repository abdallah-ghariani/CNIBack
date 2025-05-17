package com.example.demo.servecies;

import java.util.Optional;

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
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;



@Service
public class UserService implements UserDetailsService {

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

	public Page<User> getAllUsers(Pageable page, String username, String role) {
		QUser quser = new QUser("user");
		Predicate predicate = null;
		if (username != null)
			predicate = quser.username.likeIgnoreCase("%"+username+"%");
		if (role != null)
			predicate = predicate != null ? ((BooleanExpression) predicate).and(quser.role.eq(role))
					: quser.role.eq(role);

		if (predicate != null)
			return userRepository.findAll(predicate, page);

		return userRepository.findAll(page);


	}

	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user;
	}
    
    /**
     * Find a user by username
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Find a user by ID
     * @param id the user ID to search for
     * @return an Optional containing the user if found
     */
    public Optional<User> findById(String id) {
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
