package com.example.demo.controlleur;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Api;
import com.example.demo.entity.ApiRequest;
import com.example.demo.dto.ApiCreationRequestDTO;
import com.example.demo.dto.ApiRequestDTO;
import com.example.demo.dto.UserCredentialsDto;
import com.example.demo.repository.ApiRequestRepository;
import com.example.demo.servecies.ApiRequestService;
import com.example.demo.servecies.ApiService;
import com.example.demo.servecies.UserService;

@RestController
@RequestMapping("/api/api-request")
public class ApiRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestController.class);
    
    @Autowired
    private ApiRequestService apiRequestService;
    
    @Autowired
    private ApiRequestRepository apiRequestRepository;
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new API access request
     */
    @PostMapping("")
    public ResponseEntity<ApiRequest> createApiRequest(@RequestBody ApiRequestDTO requestDTO) {
        logger.info("Creating new API access request with data: {}", requestDTO);
        try {
            ApiRequest request = apiRequestService.createRequest(requestDTO);
            logger.info("Successfully created API request with ID: {}", request.getId());
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            logger.error("Error creating API request: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Create a new API access request - debug endpoint with more lenient handling
     */
    @PostMapping("/submit")
    public ResponseEntity<Object> submitApiRequest(@RequestBody Map<String, Object> requestData) {
        logger.info("Debug endpoint - Received API request submission: {}", requestData);
        
        try {
            // Extract data from the generic Map
            ApiRequestDTO dto = new ApiRequestDTO();
            
            if (requestData.containsKey("apiId")) {
                dto.setApiId(requestData.get("apiId").toString());
            }
            
            if (requestData.containsKey("name")) {
                dto.setName(requestData.get("name").toString());
            }
            
            if (requestData.containsKey("email")) {
                dto.setEmail(requestData.get("email").toString());
            }
            
            if (requestData.containsKey("secteur")) {
                dto.setSecteur(requestData.get("secteur").toString());
            }
            
            if (requestData.containsKey("structure")) {
                dto.setStructure(requestData.get("structure").toString());
            }
            
            if (requestData.containsKey("message") || requestData.containsKey("description")) {
                String messageContent = requestData.containsKey("message") ? 
                    requestData.get("message").toString() : 
                    requestData.get("description").toString();
                dto.setMessage(messageContent);
            }
            
            // Try to create the request with more error handling
            ApiRequest request;
            try {
                request = apiRequestService.createRequest(dto);
                logger.info("Successfully created API request with ID: {}", request.getId());
                return ResponseEntity.ok(request);
            } catch (Exception e) {
                logger.error("Error in service layer: {}", e.getMessage(), e);
                
                // Fallback: try direct DB insertion
                ApiRequest fallbackRequest = new ApiRequest();
                fallbackRequest.setApiId(dto.getApiId());
                fallbackRequest.setName(dto.getName());
                fallbackRequest.setEmail(dto.getEmail());
                fallbackRequest.setSecteur(dto.getSecteur());
                fallbackRequest.setStructure(dto.getStructure());
                fallbackRequest.setMessage(dto.getMessage());
                fallbackRequest.setStatus("pending");
                
                ApiRequest saved = apiRequestRepository.save(fallbackRequest);
                logger.info("Fallback: API request saved directly with ID: {}", saved.getId());
                return ResponseEntity.ok(saved);
            }
        } catch (Exception e) {
            logger.error("Critical error in API request submission: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process API request");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Get all API access requests for the authenticated provider
     */
    @GetMapping("/provider")
    public ResponseEntity<List<ApiRequest>> getProviderRequests() {
        logger.info("Getting all API access requests for current provider");
        List<ApiRequest> requests = apiRequestService.getRequestsForProvider();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get all API access requests (for admin use)
     */
    @GetMapping("/all")
    public ResponseEntity<List<ApiRequest>> getAllRequests() {
        logger.info("Getting all API access requests");
        List<ApiRequest> requests = apiRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get all API access requests made by the authenticated consumer
     */
    @GetMapping("/consumer")
    public ResponseEntity<List<ApiRequest>> getConsumerRequests() {
        logger.info("Getting all API access requests made by current consumer");
        List<ApiRequest> requests = apiRequestService.getRequestsForConsumer();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get a specific API access request
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiRequest> getRequestById(@PathVariable String requestId) {
        logger.info("Getting API access request by ID: {}", requestId);
        ApiRequest request = apiRequestService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }
    
    /**
     * Approve an API access request
     * Only the provider of the API can approve a request
     * Returns user credentials (email, username, password) for the newly created account
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<UserCredentialsDto> approveRequest(@PathVariable String requestId) {
        logger.info("Approving API access request ID: {}", requestId);
        UserCredentialsDto credentials = apiRequestService.approveRequest(requestId);
        return ResponseEntity.ok(credentials);
    }
    
    /**
     * Reject an API access request
     * Only the provider of the API can reject a request
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiRequest> rejectRequest(@PathVariable String requestId) {
        logger.info("Rejecting API access request ID: {}", requestId);
        ApiRequest rejectedRequest = apiRequestService.rejectRequest(requestId);
        return ResponseEntity.ok(rejectedRequest);
    }
    
    /**
     * Create a new API and submit an access request in one operation
     * This simplifies the process when a consumer wants to request a new API
     * that doesn't exist in the system yet
     */
    @PostMapping("/new-api")
    public ResponseEntity<Object> createApiWithRequest(@RequestBody ApiCreationRequestDTO dto) {
        logger.info("Creating new API and access request with data: {}", dto);
        try {
            // Step 1: Create the new API
            Api newApi = new Api();
            newApi.setName(dto.getApiName());
            newApi.setDescription(dto.getApiDescription());
            newApi.setUpdatedAt(new Date());
            
            // Optional API fields that might be provided
            if (dto.getApiEndpoint() != null) {
                // We would typically save the endpoint in a proper field
                // For now, we'll append it to the description
                newApi.setDescription(newApi.getDescription() + "\n\nEndpoint: " + dto.getApiEndpoint());
            }
            
            if (dto.getApiVersion() != null) {
                // Similarly for version
                newApi.setDescription(newApi.getDescription() + "\n\nVersion: " + dto.getApiVersion());
            }
            
            if (dto.getApiDocumentation() != null) {
                // Similarly for documentation URL
                newApi.setDescription(newApi.getDescription() + "\n\nDocumentation: " + dto.getApiDocumentation());
            }
            
            // Set the secteur and structure if provided
            newApi.setSecteur(dto.getSecteur());
            newApi.setStructure(dto.getStructure());
            
            // Default availability
            newApi.setAvailability(100.0);
            
            // Create the API, passing the current user so it sets providerId correctly
            Api savedApi = apiService.addApi(newApi, userService.getCurrentUser());
            logger.info("Successfully created new API with ID: {}", savedApi.getId());
            
            // Step 2: Create the access request for this API
            ApiRequestDTO requestDTO = new ApiRequestDTO();
            requestDTO.setApiId(savedApi.getId());
            requestDTO.setName(dto.getRequesterName());
            requestDTO.setEmail(dto.getRequesterEmail());
            requestDTO.setSecteur(dto.getSecteur());
            requestDTO.setStructure(dto.getStructure());
            requestDTO.setMessage(dto.getMessage());
            
            ApiRequest request = apiRequestService.createRequest(requestDTO);
            logger.info("Successfully created API request with ID: {}", request.getId());
            
            // Return both the API and request
            Map<String, Object> response = new HashMap<>();
            response.put("api", savedApi);
            response.put("request", request);
            response.put("message", "Successfully created new API and submitted access request");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating API and request: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create API and request");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
