package com.example.demo.controlleur;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AuthDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.servecies.AuthenticationService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    @Autowired
    private AuthenticationService authService;

    @PostMapping("/register")
    public String register(@RequestBody  AuthDto request) {
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody AuthDto request) {
        return authService.login(request);
    }
    
}
