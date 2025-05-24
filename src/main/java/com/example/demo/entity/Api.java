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
    
    // API endpoint details
    private String baseUrl;    // The base URL/endpoint of the API
    private String version;    // API version
    private String documentation; // Documentation URL or text
    private String swaggerUrl;  // URL to the Swagger/OpenAPI specification
    
    // Authentication details
    private String authType;     // OAuth2, API Key, etc.
    private boolean authRequired; // Whether authentication is required
    private String[] authMethods; // Array of authentication methods
    
    // Endpoint details
    private String endpointDescription; // Description of the endpoint
    private String endpointMethod;     // HTTP method (GET, POST, etc.)
    private String endpointPath;       // Path of the endpoint
    
    // Examples
    private String exampleRequest;  // Example request
    private String inputExample;   // Example input
    private String outputExample;  // Example output
    
    // Additional fields
    private boolean requiresAuth;     // Whether authentication is required
    private String[] pathParameters; // Path parameters
    
    // Response codes
    private ResponseCode[] responseCodes; // Array of response codes
    
    // Inner class for response codes
    public static class ResponseCode {
        private String statusCode;  // HTTP status code
        private String description; // Description of the status code
        private String resource;    // Resource associated with the status code
        
        public ResponseCode() {
        }
        
        public ResponseCode(String statusCode, String description, String resource) {
            this.statusCode = statusCode;
            this.description = description;
            this.resource = resource;
        }
        
        public String getStatusCode() {
            return statusCode;
        }
        
        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getResource() {
            return resource;
        }
        
        public void setResource(String resource) {
            this.resource = resource;
        }
    }
    
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
    
    public Api(String name, String description, String secteur, String structure, double availability, Date updatedAt, String providerId, String service, String baseUrl, String version, String documentation) {
        this(name, description, secteur, structure, availability, updatedAt, providerId, service);
        this.baseUrl = baseUrl;
        this.version = version;
        this.documentation = documentation;
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
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDocumentation() {
        return documentation;
    }
    
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
    
    public String getSwaggerUrl() {
        return swaggerUrl;
    }
    
    public void setSwaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
    }
    
    public String getAuthType() {
        return authType;
    }
    
    public void setAuthType(String authType) {
        this.authType = authType;
    }
    
    public boolean isAuthRequired() {
        return authRequired;
    }
    
    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }
    
    public String[] getAuthMethods() {
        return authMethods;
    }
    
    public void setAuthMethods(String[] authMethods) {
        this.authMethods = authMethods;
    }
    
    public String getEndpointDescription() {
        return endpointDescription;
    }
    
    public void setEndpointDescription(String endpointDescription) {
        this.endpointDescription = endpointDescription;
    }
    
    public String getEndpointMethod() {
        return endpointMethod;
    }
    
    public void setEndpointMethod(String endpointMethod) {
        this.endpointMethod = endpointMethod;
    }
    
    public String getEndpointPath() {
        return endpointPath;
    }
    
    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }
    
    public String getExampleRequest() {
        return exampleRequest;
    }
    
    public void setExampleRequest(String exampleRequest) {
        this.exampleRequest = exampleRequest;
    }
    
    public String getInputExample() {
        return inputExample;
    }
    
    public void setInputExample(String inputExample) {
        this.inputExample = inputExample;
    }
    
    public String getOutputExample() {
        return outputExample;
    }
    
    public void setOutputExample(String outputExample) {
        this.outputExample = outputExample;
    }
    
    public boolean isRequiresAuth() {
        return requiresAuth;
    }
    
    public void setRequiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }
    
    public String[] getPathParameters() {
        return pathParameters;
    }
    
    public void setPathParameters(String[] pathParameters) {
        this.pathParameters = pathParameters;
    }
    
    public ResponseCode[] getResponseCodes() {
        return responseCodes;
    }
    
    public void setResponseCodes(ResponseCode[] responseCodes) {
        this.responseCodes = responseCodes;
    }
}
