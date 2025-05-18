package com.example.demo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryEntity;
import com.example.demo.entity.Secteur;

@Document(collection = "users")
@QueryEntity
@JsonIgnoreProperties({"password", "authorities","accountNonLocked","credentialsNonExpired","enabled","accountNonExpired"})
public class User implements UserDetails {
    
    // Role constants
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String password;
    
    // Role of the user (e.g., "ROLE_USER", "ROLE_ADMIN")
    private String role;
    
    // Relationship with Structure
    @DocumentReference
    private Structure structure;
    
    // User's sector
    @DocumentReference
    private Secteur secteur;

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.secteur = null;
        this.structure = null;
    }
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.secteur = null;
        this.structure = null;
    }

    // Getters and setters...
    public String getRole() {
  		return role;
  	}

  	public void setRole(String role) {
  		this.role = role;
  	}
  	
  	// Role helper methods
  	public boolean isAdmin() {
  		return ROLE_ADMIN.equals(role);
  	}
  	
  	public boolean isUser() {
  		return ROLE_USER.equals(role);
  	}
  	
  	public void setAdminRole() {
  		this.role = ROLE_ADMIN;
  	}
  	
  	public void setUserRole() {
  		this.role = ROLE_USER;
  	}
  	
  	// Structure relationship getters and setters
  	public Structure getStructure() {
  		return structure;
  	}
  	
  	public void setStructure(Structure structure) {
  		this.structure = structure;
  	}
  	
  	// Sector getters and setters
  	public Secteur getSecteur() {
  		return secteur;
  	}
  	
  	public void setSecteur(Secteur secteur) {
  		this.secteur = secteur;
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
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		
		// Add role authority if present
		if(role != null) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		
		return authorities;
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
