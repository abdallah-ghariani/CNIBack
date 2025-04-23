package com.example.demo.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "adheration_requests")
public class AdherationRequest {

    @Id
    private String id;
    private String name;
    private String structure;
    private String secteur;
    private String role;
    private String message;
    private Date createdAt;
    private String status;

    public AdherationRequest() {
        this.createdAt = new Date();
        this.status = "PENDING";
    }

    public AdherationRequest(String name, String structure, String secteur, String role, String message) {
        this.name = name;
        this.structure = structure;
        this.secteur = secteur;
        this.role = role;
        this.message = message;
        this.createdAt = new Date();
        this.status = "PENDING";
    }

    // Getters and Setters
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
