package com.example.demo.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.querydsl.core.annotations.QueryEntity;

@Document(collection = "api_requests")
@QueryEntity
public class ApiRequest {
    @Id
    private String id;
    private String apiId;
    private String consumerId;
    private String providerId;   // ID of the provider who owns the API
    private String name;         // consumer name
    private String email;
    private String secteur;
    private String structure;
    private String message;
    private Date requestDate;
    private String status;       // pending/approved/rejected
    private String apiName;      // Denormalized for convenience
    private String service;      // Required for service filtering
    private String description;  // Detailed API information
    private String metadata;     // JSON string with API configuration
    
    // Additional API details
    private String baseUrl;      // API endpoint URL
    private String version;      // API version
    private String documentation; // Documentation URL
    private String swaggerUrl;   // URL to the Swagger/OpenAPI definition
    
    // Authentication details
    private String authType;     // OAuth2, API Key, etc.
    private boolean authRequired; // Whether authentication is required
    private boolean requiresAuth; // Another flag for auth requirement
    
    // Endpoint details
    private String endpointDescription; // Description of the endpoint
    private String endpointMethod;     // HTTP method (GET, POST, etc.)
    private String endpointPath;       // Path of the endpoint
    
    // Example data
    private String inputExample; // Example input/request data
    private String outputExample; // Example output/response data
    private String exampleRequest; // Example request command (e.g., curl)
    
    // Additional fields
    private String[] pathParameters; // Path parameters
    
    public ApiRequest() {
        this.requestDate = new Date();
        this.status = "pending";
    }
    
    public ApiRequest(String apiId, String consumerId, String name, String email, 
                     String secteur, String structure, String message, String apiName) {
        this.apiId = apiId;
        this.consumerId = consumerId;
        this.name = name;
        this.email = email;
        this.secteur = secteur;
        this.structure = structure;
        this.message = message;
        this.requestDate = new Date();
        this.status = "pending";
        this.apiName = apiName;
    }
    
    public ApiRequest(String apiId, String consumerId, String name, String email, 
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
    
    public String getExampleRequest() {
        return exampleRequest;
    }
    
    public void setExampleRequest(String exampleRequest) {
        this.exampleRequest = exampleRequest;
    }
    
    public boolean isRequiresAuth() {
        return requiresAuth;
    }
    
    public void setRequiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
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
    
    public String[] getPathParameters() {
        return pathParameters;
    }
    
    public void setPathParameters(String[] pathParameters) {
        this.pathParameters = pathParameters;
    }
}
