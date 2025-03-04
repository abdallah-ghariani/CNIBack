package com.example.demo.servecies;

//import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
	@Autowired
    private PasswordEncoder passwordEncoder;
    
    public String registerUser(String username, String password) {
    	User user = new User(username,passwordEncoder.encode(password));
    	try {
    	userRepository.save(user);
    	}catch(DuplicateKeyException e) {
    		throw new DuplicateKeyException("username already used");
    	}
    	return "User registred";
    }  
    
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));	
	}}
