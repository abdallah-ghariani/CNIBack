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

@Document(collection = "users")
@QueryEntity
@JsonIgnoreProperties({"password", "authorities","accountNonLocked","credentialsNonExpired","enabled","accountNonExpired"})
public class User implements UserDetails{
	
    @Id
    private String id;

	@Indexed(unique = true)
    private String username;
    private String password;
    
    // Legacy single role field (maintained for backward compatibility)
    private String role;
    
    // Multi-role support
    private boolean isAdmin;
    private boolean isProvider;
    private boolean isConsumer;
    
    // Relationship with Structure
    @DocumentReference
    private Structure structure;

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        
        // Set role flags based on the legacy role field
        if ("Admin".equals(role)) {
            this.isAdmin = true;
        } else if ("Provider".equals(role)) {
            this.isProvider = true;
        } else if ("Consumer".equals(role)) {
            this.isConsumer = true;
        }
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
  		
  		// Update role flags when setting legacy role field
  		if ("Admin".equals(role)) {
            this.isAdmin = true;
        } else if ("Provider".equals(role)) {
            this.isProvider = true;
        } else if ("Consumer".equals(role)) {
            this.isConsumer = true;
        }
  	}
  	
  	// New role flag getters and setters
  	public boolean isAdmin() {
  		return isAdmin;
  	}
  	
  	public void setAdmin(boolean isAdmin) {
  		this.isAdmin = isAdmin;
  	}
  	
  	public boolean isProvider() {
  		return isProvider;
  	}
  	
  	public void setProvider(boolean isProvider) {
  		this.isProvider = isProvider;
  	}
  	
  	public boolean isConsumer() {
  		return isConsumer;
  	}
  	
  	public void setConsumer(boolean isConsumer) {
  		this.isConsumer = isConsumer;
  	}
  	
  	// Structure relationship getters and setters
  	public Structure getStructure() {
  		return structure;
  	}
  	
  	public void setStructure(Structure structure) {
  		this.structure = structure;
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
		
		// Add legacy role if present
		if(role != null) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		
		// Add role-based authorities
		if(isAdmin) {
			authorities.add(new SimpleGrantedAuthority("Admin"));
		}
		if(isProvider) {
			authorities.add(new SimpleGrantedAuthority("Provider"));
		}
		if(isConsumer) {
			authorities.add(new SimpleGrantedAuthority("Consumer"));
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
