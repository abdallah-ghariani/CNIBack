package com.example.demo.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig { 
	
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationManager authenticationManager;
    
	
    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationManager authenticationManager) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.authenticationManager = authenticationManager;
	}



	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	
        http
        	.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        	.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests((request) -> request
            	.requestMatchers("/api/auth/**","/api/services/**","/api/structures/**","/api/secteurs/**","/api/adheration/request").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
             )
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authenticationManager(authenticationManager)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
           ;
        return http.build();
    }
    
	    CorsConfigurationSource corsConfigurationSource() {
	        CorsConfiguration configuration = new CorsConfiguration();

	        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
	        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
	        configuration.setExposedHeaders(List.of("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
	        configuration.setAllowCredentials(true);
	        configuration.setMaxAge(3600L);
	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

	        source.registerCorsConfiguration("/api/**", configuration);

	        return source;
	    }
    
    
 
    
    
}

