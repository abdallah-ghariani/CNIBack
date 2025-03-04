package com.example.demo.servecies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AuthDto;
import com.example.demo.dto.AuthResponseDto;

@Service
public class AuthenticationService {
	
	@Autowired
	private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    
    public String registerUser(AuthDto request) {
        return userService.registerUser(request.getUsername(),request.getPassword());
    }

    
    public AuthResponseDto login(AuthDto login) {
        /*Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (rawPassword.equals(user.getPassword())) {  // Compare raw password directly
                return "Login Successful";
            } else {
                return "Invalid Password";
            }
        } else {
            return "User Not Found";
        }*/
    	authenticationManager.authenticate( new UsernamePasswordAuthenticationToken( login.getUsername(), login.getPassword()));
    	String jwtToken = jwtService.generateToken(userService.loadUserByUsername(login.getUsername()));
    	return new AuthResponseDto(jwtToken) ;

      }

}
