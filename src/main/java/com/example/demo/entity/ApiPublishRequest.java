package com.example.demo.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.querydsl.core.annotations.QueryEntity;

@Document(collection = "api_publish_requests")
@QueryEntity
public class ApiPublishRequest {
    @Id
    private String id;
    private String apiId;
    private String providerId;       // ID of the provider submitting the API
    private String providerName;     // Name of the provider for denormalization
    private String apiName;          // Name of the API for denormalization
    private String description;      // Optional detailed description about the API
    private Date submissionDate;     // When the API was submitted
    private String status;           // pending/approved/rejected
    private String adminFeedback;    // Feedback from admin when approving/rejecting
    private String service;          // Service category the API belongs to
    private String metadata;         // JSON string with additional API configuration
    
    public ApiPublishRequest() {
        this.submissionDate = new Date();
        this.status = "pending";
    }
    
    public ApiPublishRequest(String apiId, String providerId, String providerName, String apiName) {
        this.apiId = apiId;
        this.providerId = providerId;
        this.providerName = providerName;
        this.apiName = apiName;
        this.submissionDate = new Date();
        this.status = "pending";
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

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminFeedback() {
        return adminFeedback;
    }

    public void setAdminFeedback(String adminFeedback) {
        this.adminFeedback = adminFeedback;
    }
    
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
