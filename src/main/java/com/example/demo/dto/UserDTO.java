package com.example.demo.dto;

/**
 * Data Transfer Object for User registration and updates
 */
public class UserDTO {
    private String username;
    private String password;
    private String role;
    private String secteurId;
    private String structureId;
    
    // Default constructor
    public UserDTO() {
    }
    
    // Constructor with all fields
    public UserDTO(String username, String password, String role, String secteurId, String structureId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.secteurId = secteurId;
        this.structureId = structureId;
    }
    
    // Getters and setters
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
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getSecteurId() {
        return secteurId;
    }
    
    public void setSecteurId(String secteurId) {
        this.secteurId = secteurId;
    }
    
    public String getStructureId() {
        return structureId;
    }
    
    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }
}
