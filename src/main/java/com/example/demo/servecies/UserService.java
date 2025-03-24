package com.example.demo.servecies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.QUser;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;



@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;


	@Autowired
	private PasswordEncoder passwordEncoder;

	public User addUser(String username, String password, String role) {
		if (userRepository.findByUsername(username).isPresent()) {
			throw new DuplicateKeyException("Username already used");
		}
		User user = new User(username, passwordEncoder.encode(password),role);
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
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
	}
}
