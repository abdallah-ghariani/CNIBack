package com.example.demo.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryEntity;

@Document(collection = "users")
@QueryEntity
@JsonIgnoreProperties({"password", "authorities","accountNonLocked","credentialsNonExpired","enabled","accountNonExpired"})
public class User implements UserDetails{
	
    @Id
    private String id;

	@Indexed(unique = true)
    private String username;
    private String password;
    private String role;

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and setters...
    public String getRole() {
  		return role;
  	}

  	public void setRole(String role) {
  		this.role = role;
  	}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if(role != null)
			return List.of(new SimpleGrantedAuthority(role));
		return List.of();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
