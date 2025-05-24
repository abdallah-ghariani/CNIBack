package com.example.demo.controlleur;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Api;
import com.example.demo.dto.ApiCreationRequestDTO;
import com.example.demo.entity.ApiRequest;
import com.example.demo.servecies.ApiRequestService;

@RestController
@RequestMapping("/api/api-creation-request")
public class ApiRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestController.class);
    
    @Autowired
    private ApiRequestService apiRequestService;
    
    /**
     * Submit a request for a new API - this creates a request only
     * When the admin approves this request, a new API will be created with 'approved' status
     */
    @PostMapping("/new-api")
    public ResponseEntity<Object> submitApiRequest(@RequestBody ApiCreationRequestDTO dto) {
        logger.info("Submitting new API creation request with data: {}", dto);
        try {
            // Create an API creation request using the service
            ApiRequest request = apiRequestService.createApiCreationRequest(dto);
            
            // Return the created request
            Map<String, Object> response = new HashMap<>();
            response.put("request", request);
            response.put("requestId", request.getId());
            response.put("message", "API creation request submitted successfully and is pending admin approval");
            response.put("status", "pending");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error submitting API creation request: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to submit API creation request");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all pending API creation requests
     * Admin only endpoint
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<ApiRequest>> getPendingRequests() {
        logger.info("Admin fetching all pending API creation requests");
        try {
            // Fetch pending API creation requests from the service
            List<ApiRequest> pendingRequests = apiRequestService.getPendingApiCreationRequests();
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            logger.error("Error fetching pending API creation requests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Approve an API creation request
     * This creates a new API with 'approved' status automatically
     */
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Object> approveRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback) {
        logger.info("Approving API creation request: {}", requestId);
        
        try {
            // Call the service to approve the request and create a new API
            Api newApi = apiRequestService.approveApiCreationRequest(requestId, feedback);
            
            // Return the created API and success message
            Map<String, Object> response = new HashMap<>();
            response.put("api", newApi);
            response.put("apiId", newApi.getId());
            response.put("message", "API creation request approved and new API created with 'approved' status");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error approving API creation request: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to approve API creation request");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Reject an API creation request
     */
    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Object> rejectRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback) {
        
        logger.info("Rejecting API creation request ID: {}", requestId);
        
        try {
            // Call the service to reject the API creation request
            ApiRequest rejectedRequest = apiRequestService.rejectApiCreationRequest(requestId, feedback);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "API creation request rejected successfully");
            response.put("requestId", rejectedRequest.getId());
            response.put("status", rejectedRequest.getStatus());
            if (feedback != null) {
                response.put("feedback", feedback);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error rejecting API creation request: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to reject API creation request");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get API creation requests made by the current user
     * @param page Page number (0-based)
     * @param size Page size
     * @param type Optional filter for request type (e.g., "CREATION", "ALL")
     * @return List of API creation requests for the current user
     */
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ApiRequest>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        
        logger.info("Getting user's API creation requests, type: {}", type);
        
        try {
            // Get user's API creation requests (paginate manually since we're using stream filtering)
            List<ApiRequest> requests = apiRequestService.getUserApiCreationRequests(type);
            
            // Manual pagination (could be improved with Spring's PageImpl)
            int start = Math.min(page * size, requests.size());
            int end = Math.min(start + size, requests.size());
            
            List<ApiRequest> pagedRequests = requests.subList(start, end);
            
            return ResponseEntity.ok(pagedRequests);
        } catch (Exception e) {
            logger.error("Error fetching user's API creation requests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
