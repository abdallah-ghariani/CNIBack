package com.example.demo.controlleur;

import com.example.demo.entity.Api;
import com.example.demo.entity.ApiRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.servecies.ApiRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/requests")
@PreAuthorize("hasAuthority('admin')")
public class AdminApiRequestController {

    private static final Logger logger = LoggerFactory.getLogger(AdminApiRequestController.class);
    
    @Autowired
    private ApiRequestService apiRequestService;

    /**
     * Get all pending API requests
     */
    @GetMapping("/pending-creation")
    public ResponseEntity<List<ApiRequest>> getPendingCreationRequests() {
        logger.info("Fetching all pending API creation requests");
        try {
            List<ApiRequest> requests = apiRequestService.getPendingApiCreationRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error fetching pending API creation requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Approve an API request
     */
    @PutMapping("/creation/{requestId}/approve")
    public ResponseEntity<Map<String, Object>> approveCreationRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback) {
        
        logger.info("Approving API creation request ID: {}", requestId);
        try {
            Api api = apiRequestService.approveApiCreationRequest(requestId, feedback);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "API creation request approved successfully",
                "api", api
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error approving API creation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "An error occurred while processing the request"
            ));
        }
    }

    /**
     * Reject an API request
     */
    @PutMapping("/creation/{requestId}/reject")
    public ResponseEntity<Map<String, String>> rejectCreationRequest(
            @PathVariable String requestId,
            @RequestParam String feedback) {
        
        logger.info("Rejecting API creation request ID: {}", requestId);
        if (feedback == null || feedback.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Feedback is required when rejecting a request"
            ));
        }

        try {
            ApiRequest rejectedRequest = apiRequestService.rejectApiCreationRequest(requestId, feedback);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "API creation request rejected successfully",
                "requestId", rejectedRequest.getId()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error rejecting API creation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "An error occurred while processing the request"
            ));
        }
    }

    /**
     * Get details of a specific API request
     */
    @GetMapping("/creation/{requestId}")
    public ResponseEntity<Map<String, Object>> getCreationRequestDetails(@PathVariable String requestId) {
        logger.info("Fetching details for API creation request ID: {}", requestId);
        try {
            // Get the request by ID
            ApiRequest request = apiRequestService.getRequestById(requestId);
            
            // Verify this is a creation request (apiId is null)
            if (request.getApiId() != null) {
                throw new BadRequestException("Not an API creation request");
            }
            
            // Return the request with a success status
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "request", request
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error",
                "message", "API creation request not found with ID: " + requestId
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error fetching API creation request details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "An error occurred while fetching the API creation request"
            ));
        }
    }
}