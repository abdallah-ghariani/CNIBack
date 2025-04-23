package com.example.demo.dto;

import org.springframework.validation.annotation.Validated;

@Validated
public class AdherationRequestDto {
    
    private String name;
    
    private String structure;
    
    private String secteur;
    
    private String role;
    
    private String message;
    
    // Default constructor
    public AdherationRequestDto() {
    }
    
    // Constructor with all fields
    public AdherationRequestDto(String name, String structure, String secteur, String role, String message) {
        this.name = name;
        this.structure = structure;
        this.secteur = secteur;
        this.role = role;
        this.message = message;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
}
