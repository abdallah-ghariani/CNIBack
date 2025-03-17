package com.example.demo.controlleur;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;

import java.security.Principal;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/name")
    public String getName(Principal principal ) {
        return principal.getName();
    }

    @GetMapping("/roles")
    public Collection<? extends GrantedAuthority> getRoles(@AuthenticationPrincipal User user ) {
        return user.getAuthorities();
    }
    
}
