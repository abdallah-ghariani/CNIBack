package com.example.demo.dto;

public class ApiResponse {
    private boolean success;
    private String message;
    private String id;
    private String error;
    
    // Default constructor
    public ApiResponse() {
    }
    
    // Success constructor
    public ApiResponse(boolean success, String message, String id) {
        this.success = success;
        this.message = message;
        this.id = id;
    }
    
    // Error constructor
    public ApiResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}
