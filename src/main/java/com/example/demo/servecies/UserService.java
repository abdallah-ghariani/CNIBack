package com.example.demo.servecies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateKeyException("Username already used");
        }
        User user = new User(username, passwordEncoder.encode(password));
        userRepository.save(user);
        return "User registered successfully";
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));} 

    public String updateUser(String id, String newPassword) {
        return userRepository.findById(id).map(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return "User updated successfully";
        }).orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
    }

    public String deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        return "User deleted successfully";
    }
    public List<User>getAllUsers(){
    	return this.userRepository.findAll();
  
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
