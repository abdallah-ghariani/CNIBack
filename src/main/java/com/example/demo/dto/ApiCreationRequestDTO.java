package com.example.demo.dto;

import java.util.Date;
import java.util.List;

/**
 * DTO for creating a new API and requesting access in one operation
 * Updated to support nested API object structure from frontend
 */
public class ApiCreationRequestDTO {
    
    // Request metadata
    private String requestType;     // Type of request (create, etc.)
    private Date requestDate;
    private String reason;          // Reason for the request
    
    // User information
    private String requesterName;   // Consumer name
    private String requesterEmail;  // Consumer email
    private String message;         // Message from requester
    
    // Nested API object
    private ApiDetails api;
    
    /**
     * Inner class to represent the nested API details
     */
    public static class ApiDetails {
        private String name;
        private String description;
        private String baseUrl;
        private String version;
        private String status;
        private String secteur;
        private String structure;
        private String service;
        private String providerId;
        private List<String> inputExamples;
        private List<String> outputExamples;
        private String inputExample;
        private String outputExample;
        private List<String> endpoints;
        private String documentation;
        private String swaggerUrl;
        private String authentication;
        private String authType;
        private String authUrl;
        private String authDetails;
        private String visibility;
        private String accessRestrictions;
        private List<String> allowedDomains;
        private String rateLimits;
        private List<String> tags;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public List<String> getInputExamples() {
            return inputExamples;
        }

        public void setInputExamples(List<String> inputExamples) {
            this.inputExamples = inputExamples;
        }

        public List<String> getOutputExamples() {
            return outputExamples;
        }

        public void setOutputExamples(List<String> outputExamples) {
            this.outputExamples = outputExamples;
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

        public List<String> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<String> endpoints) {
            this.endpoints = endpoints;
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

        public String getAuthentication() {
            return authentication;
        }

        public void setAuthentication(String authentication) {
            this.authentication = authentication;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getAuthUrl() {
            return authUrl;
        }

        public void setAuthUrl(String authUrl) {
            this.authUrl = authUrl;
        }

        public String getAuthDetails() {
            return authDetails;
        }

        public void setAuthDetails(String authDetails) {
            this.authDetails = authDetails;
        }

        public String getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = visibility;
        }

        public String getAccessRestrictions() {
            return accessRestrictions;
        }

        public void setAccessRestrictions(String accessRestrictions) {
            this.accessRestrictions = accessRestrictions;
        }

        public List<String> getAllowedDomains() {
            return allowedDomains;
        }

        public void setAllowedDomains(List<String> allowedDomains) {
            this.allowedDomains = allowedDomains;
        }

        public String getRateLimits() {
            return rateLimits;
        }

        public void setRateLimits(String rateLimits) {
            this.rateLimits = rateLimits;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }
    
    /**
     * Default constructor
     */
    public ApiCreationRequestDTO() {
        this.requestDate = new Date();
        this.api = new ApiDetails();
        this.api.setStatus("pending");
    }
    
    // Getters and setters for main class
    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApiDetails getApi() {
        return api;
    }

    public void setApi(ApiDetails api) {
        this.api = api;
    }
    
    // Direct fields for flat structure support
    private String apiName;
    private String description;
    private String baseUrl;
    private String version;
    private String documentation;
    private String status;
    private String secteur;
    private String structure;
    private String service;
    private String authType;
    private String swaggerUrl;
    private String inputExample;
    private String outputExample;
    private String exampleRequest;
    private Boolean authRequired;
    private String endpointDescription;
    private String endpointMethod;
    private String endpointPath;
    private Boolean requiresAuth;
    private String[] pathParameters;
    private Object[] responseCodes;
    
    // Helper methods that work with both flat and nested structure
    public String getApiName() {
        // First try to get from direct field, then from nested api object
        return apiName != null ? apiName : (api != null ? api.getName() : null);
    }
    
    public void setApiName(String apiName) {
        this.apiName = apiName;
        // Also set in nested object if it exists
        if (api != null) {
            api.setName(apiName);
        }
    }
    
    public String getDescription() {
        return description != null ? description : (api != null ? api.getDescription() : null);
    }
    
    public void setDescription(String description) {
        this.description = description;
        if (api != null) {
            api.setDescription(description);
        }
    }
    
    public String getBaseUrl() {
        return baseUrl != null ? baseUrl : (api != null ? api.getBaseUrl() : null);
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        if (api != null) {
            api.setBaseUrl(baseUrl);
        }
    }
    
    public String getVersion() {
        return version != null ? version : (api != null ? api.getVersion() : null);
    }
    
    public void setVersion(String version) {
        this.version = version;
        if (api != null) {
            api.setVersion(version);
        }
    }
    
    public String getDocumentation() {
        return documentation != null ? documentation : (api != null ? api.getDocumentation() : null);
    }
    
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
        if (api != null) {
            api.setDocumentation(documentation);
        }
    }
    
    public String getStatus() {
        return status != null ? status : (api != null ? api.getStatus() : null);
    }
    
    public void setStatus(String status) {
        this.status = status;
        if (api != null) {
            api.setStatus(status);
        }
    }
    
    public String getSecteur() {
        return secteur != null ? secteur : (api != null ? api.getSecteur() : null);
    }
    
    public void setSecteur(String secteur) {
        this.secteur = secteur;
        if (api != null) {
            api.setSecteur(secteur);
        }
    }
    
    public String getStructure() {
        return structure != null ? structure : (api != null ? api.getStructure() : null);
    }
    
    public void setStructure(String structure) {
        this.structure = structure;
        if (api != null) {
            api.setStructure(structure);
        }
    }
    
    public String getService() {
        return service != null ? service : (api != null ? api.getService() : null);
    }
    
    public void setService(String service) {
        this.service = service;
        if (api != null) {
            api.setService(service);
        }
    }
    
    public String getAuthType() {
        return authType != null ? authType : (api != null ? api.getAuthType() : null);
    }
    
    public void setAuthType(String authType) {
        this.authType = authType;
        if (api != null) {
            api.setAuthType(authType);
        }
    }
    
    public String getSwaggerUrl() {
        return swaggerUrl != null ? swaggerUrl : (api != null ? api.getSwaggerUrl() : null);
    }
    
    public void setSwaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
        if (api != null) {
            api.setSwaggerUrl(swaggerUrl);
        }
    }
    
    public String getInputExample() {
        return inputExample != null ? inputExample : (api != null ? api.getInputExample() : null);
    }
    
    public void setInputExample(String inputExample) {
        this.inputExample = inputExample;
        if (api != null) {
            api.setInputExample(inputExample);
        }
    }
    
    public String getOutputExample() {
        return outputExample != null ? outputExample : (api != null ? api.getOutputExample() : null);
    }
    
    public void setOutputExample(String outputExample) {
        this.outputExample = outputExample;
        if (api != null) {
            api.setOutputExample(outputExample);
        }
    }
    
    public String getExampleRequest() {
        return exampleRequest;
    }
    
    public void setExampleRequest(String exampleRequest) {
        this.exampleRequest = exampleRequest;
    }
    
    public Boolean getAuthRequired() {
        return authRequired;
    }
    
    public void setAuthRequired(Boolean authRequired) {
        this.authRequired = authRequired;
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
    
    public Boolean getRequiresAuth() {
        return requiresAuth;
    }
    
    public void setRequiresAuth(Boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }
    
    public String[] getPathParameters() {
        return pathParameters;
    }
    
    public void setPathParameters(String[] pathParameters) {
        this.pathParameters = pathParameters;
    }
    
    public Object[] getResponseCodes() {
        return responseCodes;
    }
    
    public void setResponseCodes(Object[] responseCodes) {
        this.responseCodes = responseCodes;
    }
    
    // Helper methods for backward compatibility with service layer
    public String getName() {
        return requesterName;
    }
    
    public String getEmail() {
        return requesterEmail;
    }
}
