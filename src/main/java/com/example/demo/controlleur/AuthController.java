package com.example.demo.controlleur;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.example.demo.dto.AuthDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.servecies.AuthenticationService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    @Autowired
    private AuthenticationService authService;

    /*@PostMapping("/register")
    public String register(@RequestBody  AuthDto request) {
        return authService.registerUser(request);
    }*/

    @PostMapping("/login")
    public AuthResponseDto login(HttpServletResponse response , @RequestBody AuthDto request) {
        var authResponse = authService.login(request);
        var cookie = new Cookie("refreshToken", authService.getRefreshToken(authResponse.getToken()));
        cookie.setHttpOnly(true);
        cookie.setMaxAge(24*3600);
        response.addCookie(cookie);
        return authResponse;
    }
    
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
    	var cookie = new Cookie("refreshToken", null);
    	cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
    
    @PostMapping("/refreshToken")
    public AuthResponseDto refreshToken(
            @CookieValue(required = false) String refreshToken,
            @RequestBody(required = false) Map<String, String> requestBody,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Try to get token from different sources
        String token = null;
        
        // 1. Check cookie first
        if (refreshToken != null && !refreshToken.isEmpty()) {
            token = refreshToken;
        }
        // 2. Check request body
        else if (requestBody != null && requestBody.containsKey("refreshToken")) {
            token = requestBody.get("refreshToken");
        }
        // 3. Check Authorization header (Bearer format)
        else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        // If no token found, throw exception
        if (token == null || token.isEmpty()) {
            throw new AccessDeniedException("RefreshToken is missing");
        }
        
        return authService.refreshAccessToken(token);
    }
    
}
