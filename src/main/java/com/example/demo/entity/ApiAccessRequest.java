package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.querydsl.core.annotations.QueryEntity;

@Document(collection = "api_access_requests")
@QueryEntity
public class ApiAccessRequest {
    @Id
    private String id;
    private String apiId;
    private String consumerId;
    private String providerId;   // ID of the provider who owns the API
    private String name;         // consumer name
    private String email;
    private String secteur;      // Requester's sector
    private String structure;
    private String message;
    private Date requestDate;
    private String status;       // PENDING/APPROVED/REJECTED
    private String apiName;      // Denormalized for convenience
    private String apiSector;    // API's sector for access control
    private String requesterSector; // Requester's sector for filtering
    private String approvedById;  // User ID who approved the request
    private String rejectedById;  // User ID who rejected the request
    private LocalDateTime approvalDate;  // When the request was approved
    private LocalDateTime rejectionDate; // When the request was rejected
    private String feedback;      // Feedback from approver/rejector
    private String service;      // Required for service filtering
    private String description;  // Detailed API information
    private String metadata;     // JSON string with API configuration
    
    public ApiAccessRequest() {
        this.requestDate = new Date();
        this.status = "PENDING";
    }
    
    public ApiAccessRequest(String apiId, String consumerId, String name, String email, 
                     String secteur, String structure, String message, String apiName) {
        this();
        this.apiId = apiId;
        this.consumerId = consumerId;
        this.name = name;
        this.email = email;
        this.secteur = secteur;
        this.structure = structure;
        this.message = message;
        this.apiName = apiName;
    }
    
    public ApiAccessRequest(String apiId, String consumerId, String name, String email, 
                     String secteur, String structure, String message, String apiName,
                     String service, String description, String metadata) {
        this(apiId, consumerId, name, email, secteur, structure, message, apiName);
        this.service = service;
        this.description = description;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
    
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getApiSector() {
        return apiSector;
    }

    public void setApiSector(String apiSector) {
        this.apiSector = apiSector;
    }

    public String getRequesterSector() {
        return requesterSector;
    }

    public void setRequesterSector(String requesterSector) {
        this.requesterSector = requesterSector;
    }

    public String getApprovedById() {
        return approvedById;
    }

    public void setApprovedById(String approvedById) {
        this.approvedById = approvedById;
    }

    public String getRejectedById() {
        return rejectedById;
    }

    public void setRejectedById(String rejectedById) {
        this.rejectedById = rejectedById;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDateTime getRejectionDate() {
        return rejectionDate;
    }

    public void setRejectionDate(LocalDateTime rejectionDate) {
        this.rejectionDate = rejectionDate;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
