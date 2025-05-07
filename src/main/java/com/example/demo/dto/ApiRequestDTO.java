package com.example.demo.dto;

/**
 * Data Transfer Object for API requests
 */
public class ApiRequestDTO {
    private String apiId;
    private String name;
    private String email;
    private String secteur;
    private String structure;
    private String message;
    
    public ApiRequestDTO() {
    }
    
    public String getApiId() {
        return apiId;
    }
    
    public void setApiId(String apiId) {
        this.apiId = apiId;
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
    
    @Override
    public String toString() {
        return "ApiRequestDTO [apiId=" + apiId + ", name=" + name + ", email=" + email + ", secteur=" + secteur
                + ", structure=" + structure + ", message=" + message + "]";
    }
}
