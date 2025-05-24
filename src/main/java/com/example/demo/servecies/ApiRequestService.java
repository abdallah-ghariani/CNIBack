package com.example.demo.servecies;

// Imports
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ApiCreationRequestDTO;
import com.example.demo.dto.ApiRequestDTO;
import com.example.demo.dto.UserCredentialsDto;
import com.example.demo.entity.Api;
import com.example.demo.entity.ApiRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ApiRepository;
import com.example.demo.repository.ApiRequestRepository;

@Service
public class ApiRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiRequestService.class);
    
    @Autowired
    private ApiRequestRepository apiRequestRepository;
    
    @Autowired
    private ApiRepository apiRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new API access request
     * This method now supports both authenticated and unauthenticated users
     */
    public ApiRequest createRequest(ApiRequestDTO dto) {
        // Verify the API exists
        Api api = apiRepository.findById(dto.getApiId())
            .orElseThrow(() -> new ResourceNotFoundException("API not found with ID: " + dto.getApiId()));
        
        // Create the request
        ApiRequest request = new ApiRequest();
        request.setApiId(dto.getApiId());
        
        // Try to get current user if authenticated
        String consumerId = null;
        try {
            User consumer = userService.getCurrentUser();
            consumerId = consumer.getId();
            logger.info("Creating new API access request for API ID: {} by authenticated consumer: {}", 
                      dto.getApiId(), consumer.getUsername());
        } catch (Exception e) {
            // User is not authenticated or there was an error getting the current user
            logger.info("Creating new API access request for API ID: {} by unauthenticated user with email: {}", 
                      dto.getApiId(), dto.getEmail());
        }
        
        // Set consumer ID if available
        request.setConsumerId(consumerId);
    
        
        // Set basic fields
        request.setName(dto.getName());
        request.setEmail(dto.getEmail());
        request.setSecteur(dto.getSecteur());
        request.setStructure(dto.getStructure());
        request.setMessage(dto.getMessage());
        
        // Set additional fields from updated DTO
        request.setService(dto.getService());       // Service filtering
     
        
        
        // Set request date (either from DTO or current date)
        if (dto.getRequestDate() != null) {
            request.setRequestDate(dto.getRequestDate());
        } else {
            request.setRequestDate(new Date());
        }
        
        // Set status (either from DTO or default to "pending")
        if (dto.getStatus() != null) {
            request.setStatus(dto.getStatus());
        } else {
            request.setStatus("pending");
        }
        
        return apiRequestRepository.save(request);
    }
    
    /**
     * Get all API access requests for APIs owned by the current provider
     * Returns all requests (pending, approved, rejected) for complete history
     */
    public List<ApiRequest> getRequestsForProvider() {
        // Get current user
        User provider = userService.getCurrentUser();
        logger.info("Getting all API access requests for provider: {}", provider.getUsername());
        
        // Method 1: Get requests by providerId directly (primary method)
        List<ApiRequest> requestsByProviderId = apiRequestRepository.findByProviderId(provider.getId());
        logger.info("Found {} API access requests by provider ID directly", requestsByProviderId.size());
        
        // Method 2: Get all APIs owned by this provider
        List<Api> providerApis = apiRepository.findByProviderId(provider.getId());
        
        if (!providerApis.isEmpty()) {
            // Get all API IDs
            List<String> apiIds = providerApis.stream()
                .map(Api::getId)
                .collect(Collectors.toList());
            
            // Find all requests for these APIs (regardless of status)
            List<ApiRequest> requestsByApiIds = apiRequestRepository.findByApiIdIn(apiIds);
            logger.info("Found {} API access requests by API IDs", requestsByApiIds.size());
            
            // Combine both result sets and remove duplicates
            if (!requestsByApiIds.isEmpty()) {
                requestsByProviderId.addAll(requestsByApiIds);
                // Remove duplicates by converting to a Set and back to List
                requestsByProviderId = requestsByProviderId.stream()
                    .distinct()
                    .collect(Collectors.toList());
            }
        } else {
            logger.info("No APIs found for provider: {}", provider.getUsername());
        }
        
        // For each request, ensure we have all the required details
        for (ApiRequest request : requestsByProviderId) {
            // Make sure we have the API name
            if (request.getApiName() == null || request.getApiName().isEmpty()) {
                try {
                    Api api = apiRepository.findById(request.getApiId())
                        .orElse(null);
                    if (api != null) {
                        request.setApiName(api.getName());
                    }
                } catch (Exception e) {
                    logger.warn("Could not fetch API name for request {}", request.getId());
                }
            }
        }
        
        logger.info("Found total of {} API access requests for provider: {}", requestsByProviderId.size(), provider.getUsername());
        return requestsByProviderId;
    }
    
    /**
     * Get API access requests for a specific API
     */
    public List<ApiRequest> getRequestsByApiId(String apiId) {
        logger.info("Getting all API access requests for API ID: {}", apiId);
        return apiRequestRepository.findByApiId(apiId);
    }
    
    /**
     * Get API access requests for a specific consumer
     */
    public List<ApiRequest> getRequestsByConsumerId(String consumerId) {
        logger.info("Getting all API access requests from consumer ID: {}", consumerId);
        return apiRequestRepository.findByConsumerId(consumerId);
    }
    
    /**
     * Get all API requests, regardless of status, consumer, or provider
     * This is useful for admin views and testing
     */
    public List<ApiRequest> getAllRequests() {
        logger.info("Getting all API requests");
        return apiRequestRepository.findAll();
    }
    
    /**
     * Get all API access requests made by the current authenticated consumer
     * Returns all requests regardless of status (pending, approved, rejected)
     */
    public List<ApiRequest> getRequestsForConsumer() {
        // Get current user
        User consumer = userService.getCurrentUser();
        logger.info("Getting all API access requests made by consumer: {}", consumer.getUsername());
        
        // Find all requests made by this consumer
        List<ApiRequest> requests = apiRequestRepository.findByConsumerId(consumer.getId());
        
        // For each request, ensure we have the API name
        for (ApiRequest request : requests) {
            if (request.getApiName() == null || request.getApiName().isEmpty()) {
                try {
                    Api api = apiRepository.findById(request.getApiId())
                        .orElse(null);
                    if (api != null) {
                        request.setApiName(api.getName());
                    }
                } catch (Exception e) {
                    logger.warn("Could not fetch API name for request {}", request.getId());
                }
            }
        }
        
        logger.info("Found {} API access requests for consumer: {}", requests.size(), consumer.getUsername());
        return requests;
    }
    
    /**
     * Get an API access request by ID
     */
    public ApiRequest getRequestById(String requestId) {
        logger.info("Getting API access request by ID: {}", requestId);
        return apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("API request not found with ID: " + requestId));
    }
    
    
    
    /**
     * Generates a secure random password
     *
    
    /**
     * Reject an API access request
     * Only the provider of the API can reject a request
     */
    public ApiRequest rejectRequest(String requestId) {
        User provider = userService.getCurrentUser();
        logger.info("Rejecting API access request ID: {} by provider: {}", 
                   requestId, provider.getUsername());
        
        // Find the request
        ApiRequest request = apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));
            
        // Verify the API belongs to this provider
        Api api = apiRepository.findById(request.getApiId())
            .orElseThrow(() -> new ResourceNotFoundException("API not found with ID: " + request.getApiId()));
        
        // Check if providerId is set and matches current user
        String providerId = api.getProviderId();
        if (providerId == null) {
            // If providerId is not set, log a warning and use the request's providerId if available
            logger.warn("API {} does not have a providerId set, using providerId from request", api.getId());
            providerId = request.getProviderId();
        }
        
        if (providerId == null || !providerId.equals(provider.getId())) {
            throw new UnauthorizedException("Not authorized to reject this request");
        }
        
        // Check if request is already processed
        if (!request.getStatus().equals("pending")) {
            throw new BadRequestException("Request is already " + request.getStatus());
        }
        
        // Update request status
        request.setStatus("rejected");
        
        return apiRequestRepository.save(request);
    }
    
    /**
     * Create a new API creation request
     * This is used when a user wants to submit a request to create a new API
     * The API is not created immediately but only when approved by an admin
     * Updated to handle nested API object structure
     * 
     * @param dto The DTO containing details for the new API
     * @return The created API creation request
     */
    public ApiRequest createApiCreationRequest(ApiCreationRequestDTO dto) {
        logger.info("Creating new API creation request with data: {}", dto);
        
        // Get the current user
        User user = userService.getCurrentUser();
        
        // Create a new request
        ApiRequest request = new ApiRequest();
        
        // Map requester information (try both name and requesterName)
        if (dto.getName() != null) {
            request.setName(dto.getName());
        } else {
            request.setName(dto.getRequesterName());
        }
        
        if (dto.getEmail() != null) {
            request.setEmail(dto.getEmail());
        } else {
            request.setEmail(dto.getRequesterEmail());
        }
        
        request.setMessage(dto.getMessage() != null ? dto.getMessage() : "Request to create new API");
        
        // Map API details using getter methods that check both flat and nested structure
        request.setApiName(dto.getApiName());
        request.setDescription(dto.getDescription());
        request.setSecteur(dto.getSecteur());
        request.setStructure(dto.getStructure());
        request.setService(dto.getService());
        
        // Map additional API details
        request.setBaseUrl(dto.getBaseUrl());
        request.setVersion(dto.getVersion());
        request.setDocumentation(dto.getDocumentation());
        request.setSwaggerUrl(dto.getSwaggerUrl());
        
        // Map authentication details
        request.setAuthType(dto.getAuthType());
        request.setAuthRequired(dto.getAuthRequired() != null ? dto.getAuthRequired() : false);
        
        // Map example data
        request.setInputExample(dto.getInputExample());
        request.setOutputExample(dto.getOutputExample());
        request.setExampleRequest(dto.getExampleRequest());
        
        // Map endpoint details
        if (dto.getEndpointDescription() != null) {
            request.setEndpointDescription(dto.getEndpointDescription());
        }
        if (dto.getEndpointMethod() != null) {
            request.setEndpointMethod(dto.getEndpointMethod());
        }
        if (dto.getEndpointPath() != null) {
            request.setEndpointPath(dto.getEndpointPath());
        }
        
        // Map authorization details
        if (dto.getRequiresAuth() != null) {
            request.setRequiresAuth(dto.getRequiresAuth().booleanValue());
        }
        
        // We no longer need to use metadata since all fields are stored directly
        // Just set an empty metadata field or use it for any additional JSON data
        request.setMetadata("");
        
        // Set the requester as provider
        if (user != null) {
            request.setProviderId(user.getId());
        }
        
        // Mark as a creation request by setting apiId to null
        request.setApiId(null);
        
        // Use status from DTO or default to pending
        request.setStatus(dto.getStatus() != null ? dto.getStatus() : "pending");
        
        // Use request date from DTO or default to current date
        request.setRequestDate(dto.getRequestDate() != null ? dto.getRequestDate() : new Date());
        
        // Save and return the request
        return apiRequestRepository.save(request);
    }
    
    /**
     * Approve an API creation request
     * This will create a new API with 'approved' status
     * Only admins can approve creation requests
     * Updated to store endpoint information in dedicated fields
     * 
     * @param requestId The ID of the API creation request to approve
     * @param feedback Optional feedback message
     * @return The created API
     */
    public Api approveApiCreationRequest(String requestId, String feedback) {
        logger.info("Approving API creation request ID: {}", requestId);
        
        // Find the request
        ApiRequest request = apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("API creation request not found with ID: " + requestId));
        
        // Check if it's a pending request
        if (!"pending".equals(request.getStatus())) {
            throw new BadRequestException("Cannot approve request with status: " + request.getStatus());
        }
        
        try {
            // Create a new API based on the request data
            Api newApi = new Api();
            
            // Transfer basic API information
            newApi.setName(request.getApiName());
            newApi.setDescription(request.getDescription());
            newApi.setSecteur(request.getSecteur());
            newApi.setStructure(request.getStructure());
            
            // Transfer API endpoint details
            newApi.setBaseUrl(request.getBaseUrl());
            newApi.setVersion(request.getVersion());
            newApi.setDocumentation(request.getDocumentation());
            newApi.setSwaggerUrl(request.getSwaggerUrl());
            
            // Transfer authentication details
            newApi.setAuthType(request.getAuthType());
            newApi.setAuthRequired(request.isAuthRequired());
            newApi.setRequiresAuth(request.isRequiresAuth());
            
            // Transfer endpoint details
            newApi.setEndpointDescription(request.getEndpointDescription());
            newApi.setEndpointMethod(request.getEndpointMethod());
            newApi.setEndpointPath(request.getEndpointPath());
            
            // Transfer example data
            newApi.setInputExample(request.getInputExample());
            newApi.setOutputExample(request.getOutputExample());
            newApi.setExampleRequest(request.getExampleRequest());
            
            // Transfer additional fields
            newApi.setPathParameters(request.getPathParameters());
            
            // Note: We no longer need to parse metadata as we're directly transferring all fields
            
            // Set the service field from the request
            if (request.getService() != null) {
                newApi.setService(request.getService());
                logger.info("Setting service ID: {} for new API", request.getService());
            }
            
            // Note: We're using the metadata to extract fields instead of trying to access the original DTO
            // The metadata already contains the key information we need for the API
            
            // Set additional defaults as needed
            newApi.setAuthRequired(false);   // Default: auth not required unless specified
            newApi.setRequiresAuth(false);   // Default: auth not required unless specified
            
            // Set default HTTP method if not specified
            newApi.setEndpointMethod("GET");  // Default method
            
            // In the future, if we need to capture more details from the original request,
            // we can consider storing the original request DTO in a cache or database
            
            // Set API provider to the original requester
            newApi.setProviderId(request.getProviderId());
            
            // Set approval status directly to "approved"
            newApi.setApprovalStatus("approved");
            
            // Default availability
            newApi.setAvailability(100.0);
            newApi.setUpdatedAt(new Date());
            
            // Save the new API
            Api savedApi = apiRepository.save(newApi);
            
            // Update the request status
            request.setStatus("approved");
            request.setApiId(savedApi.getId()); // Link to the newly created API
            apiRequestRepository.save(request);
            
            logger.info("Created new API from request: {} with ID: {}", requestId, savedApi.getId());
            
            return savedApi;
        } catch (Exception e) {
            logger.error("Error approving API creation request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create API from request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reject an API creation request
     * Only admins can reject creation requests
     * 
     * @param requestId The ID of the API creation request to reject
     * @param feedback Optional feedback message
     * @return The rejected request
     */
    public ApiRequest rejectApiCreationRequest(String requestId, String feedback) {
        logger.info("Rejecting API creation request ID: {}", requestId);
        
        // Find the request
        ApiRequest request = apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("API creation request not found with ID: " + requestId));
        
        // Check if it's a pending request
        if (!"pending".equals(request.getStatus())) {
            throw new BadRequestException("Cannot reject request with status: " + request.getStatus());
        }
        
        // Update status to rejected
        request.setStatus("rejected");
        
        // Store feedback in metadata if provided
        if (feedback != null && !feedback.isEmpty()) {
            String currentMetadata = request.getMetadata() != null ? request.getMetadata() : "";
            request.setMetadata(currentMetadata + "\n\nRejection Reason: " + feedback);
        }
        
        // Save and return the updated request
        return apiRequestRepository.save(request);
    }
    
    /**
     * Get all pending API creation requests
     * This is used by admins to view requests that need approval
     * 
     * @return List of pending API creation requests
     */
    public List<ApiRequest> getPendingApiCreationRequests() {
        logger.info("Getting all pending API creation requests");
        
        // Find requests where apiId is null (creation requests) and status is "pending"
        return apiRequestRepository.findAll().stream()
            .filter(req -> req.getApiId() == null && "pending".equals(req.getStatus()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get API creation requests for the current user
     * This is used to show users their own API creation requests
     * 
     * @param type Optional filter for request type (e.g., "CREATION", "ALL")
     * @return List of API creation requests for the current user
     */
    public List<ApiRequest> getUserApiCreationRequests(String type) {
        User currentUser = userService.getCurrentUser();
        logger.info("Getting API creation requests for user: {}, type: {}", currentUser.getUsername(), type);
        
        // Get all requests for the current user
        List<ApiRequest> userRequests = apiRequestRepository.findByProviderId(currentUser.getId());
        
        // Filter based on request type
        if ("CREATION".equalsIgnoreCase(type)) {
            // Return only creation requests (apiId is null)
            return userRequests.stream()
                .filter(req -> req.getApiId() == null)
                .collect(Collectors.toList());
        } else {
            // Return all requests for this user
            return userRequests;
        }
    }
}
