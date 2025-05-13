package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "adherations")
public class Adheration {
    
    @Id
    private String id;
    private String name;
    private String email;
    private String structure;
    private String secteur;
    private String role;
    private String message;
    private String status;
    
    public Adheration() {
        // Default status is PENDING for new requests
        this.status = "PENDING";
    }
    
    public Adheration(String name, String email, String structure, String secteur, String role, String message) {
        this.name = name;
        this.email = email;
        this.structure = structure;
        this.secteur = secteur;
        this.role = role;
        this.message = message;
        this.status = "PENDING";
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getStructure() {
        return structure;
    }
    
    public void setStructure(String structure) {
        this.structure = structure;
    }
    
    public String getSecteur() {
        return secteur;
    }
    
    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
