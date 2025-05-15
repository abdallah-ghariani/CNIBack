package com.example.demo.controlleur;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ApiPublishRequest;
import com.example.demo.dto.ApiPublishRequestDTO;
import com.example.demo.servecies.ApiPublishRequestService;

@RestController
@RequestMapping("/api/api-publish-request")
public class ApiPublishRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ApiPublishRequestController.class);
    
    @Autowired
    private ApiPublishRequestService apiPublishRequestService;
    
    /**
     * Create a new API publish request
     */
    @PostMapping("")
    public ResponseEntity<ApiPublishRequest> createPublishRequest(@RequestBody ApiPublishRequestDTO requestDTO) {
        logger.info("Creating new API publish request with data: {}", requestDTO);
        try {
            ApiPublishRequest request = apiPublishRequestService.createPublishRequest(requestDTO);
            logger.info("Successfully created API publish request with ID: {}", request.getId());
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            logger.error("Error creating API publish request: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get all API publish requests for the authenticated provider
     */
    @GetMapping("/provider")
    public ResponseEntity<List<ApiPublishRequest>> getProviderRequests() {
        logger.info("Getting all API publish requests for current provider");
        List<ApiPublishRequest> requests = apiPublishRequestService.getRequestsForProvider();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get all API publish requests (for admin use)
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<ApiPublishRequest>> getAllRequests() {
        logger.info("Getting all API publish requests");
        List<ApiPublishRequest> requests = apiPublishRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get pending API publish requests (for admin use)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<ApiPublishRequest>> getPendingRequests() {
        logger.info("Getting pending API publish requests");
        List<ApiPublishRequest> requests = apiPublishRequestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get a specific API publish request
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiPublishRequest> getRequestById(@PathVariable String requestId) {
        logger.info("Getting API publish request by ID: {}", requestId);
        ApiPublishRequest request = apiPublishRequestService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }
    
    /**
     * Approve an API publish request (admin only)
     */
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiPublishRequest> approveRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback) {
        logger.info("Approving API publish request ID: {}", requestId);
        ApiPublishRequest approvedRequest = apiPublishRequestService.approveRequest(requestId, feedback);
        return ResponseEntity.ok(approvedRequest);
    }
    
    /**
     * Reject an API publish request (admin only)
     */
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiPublishRequest> rejectRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback) {
        logger.info("Rejecting API publish request ID: {}", requestId);
        ApiPublishRequest rejectedRequest = apiPublishRequestService.rejectRequest(requestId, feedback);
        return ResponseEntity.ok(rejectedRequest);
    }
}
