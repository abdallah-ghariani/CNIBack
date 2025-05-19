package com.example.demo.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.querydsl.core.annotations.QueryEntity;

@Document(collection = "apis")
@QueryEntity
public class Api {
    @Id
    private String id;
    private String name;
    private String secteur;
    private String structure;
    private String description;
    private double availability;
    private Date updatedAt;
    private String approvalStatus; // pending, approved, rejected
    private String providerId; // ID of the provider who owns this API
    private String service; // ID of the service this API belongs to
    
    public Api() {
    }

    public Api(String name, String description, String secteur, String structure, double availability, Date updatedAt, String providerId, String service) {
        this.name = name;
        this.description = description;
        this.secteur = secteur;
        this.structure = structure;
        this.availability = availability;
        this.updatedAt = updatedAt;
        this.approvalStatus = "pending";
        this.providerId = providerId;
        this.service = service;
    }

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

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAvailability() {
        return availability;
    }

    public void setAvailability(double availability) {
        this.availability = availability;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
