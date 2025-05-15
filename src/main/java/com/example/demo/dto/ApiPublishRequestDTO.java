package com.example.demo.dto;

public class ApiPublishRequestDTO {
    private String apiId;
    private String apiName;
    private String description;
    private String service;
    private String metadata;
    
    public ApiPublishRequestDTO() {
    }
    
    public String getApiId() {
        return apiId;
    }
    
    public void setApiId(String apiId) {
        this.apiId = apiId;
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
