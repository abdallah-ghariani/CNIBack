package com.example.demo.entity;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity representing an activity or event in the system
 */
@Document(collection = "activities")
public class Activity {
    
    @Id
    private String id;
    
    private ActivityType type;
    private String description;
    private Date timestamp;
    private String entityId;
    private String entityName;
    private String userId;
    private String username;
    private String sectorId;
    private String sectorName;
    private String serviceId;
    private String serviceName;
    private String structureId;
    private String structureName;
    private Map<String, Object> details;
    
    // Default constructor
    public Activity() {
        this.timestamp = new Date();
    }
    
    // Constructor with type and description
    public Activity(ActivityType type, String description) {
        this.type = type;
        this.description = description;
        this.timestamp = new Date();
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSectorId() {
        return sectorId;
    }

    public void setSectorId(String sectorId) {
        this.sectorId = sectorId;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStructureId() {
        return structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public String getStructureName() {
        return structureName;
    }

    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    @Override
    public String toString() {
        return "Activity [id=" + id + ", type=" + type + ", description=" + description + ", timestamp=" + timestamp
                + ", entityName=" + entityName + ", sectorName=" + sectorName + "]";
    }
}
