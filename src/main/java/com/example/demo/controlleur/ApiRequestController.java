package com.example.demo.controlleur;

import java.util.List;

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

import com.example.demo.entity.ApiRequest;
import com.example.demo.dto.ApiRequestDTO;
import com.example.demo.dto.UserCredentialsDto;
import com.example.demo.servecies.ApiRequestService;

@RestController
@RequestMapping("/api/api-request")
public class ApiRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestController.class);
    
    @Autowired
    private ApiRequestService apiRequestService;
    
    /**
     * Create a new API access request
     */
    @PostMapping("")
    public ResponseEntity<ApiRequest> createApiRequest(@RequestBody ApiRequestDTO requestDTO) {
        logger.info("Creating new API access request with data: {}", requestDTO);
        ApiRequest request = apiRequestService.createRequest(requestDTO);
        return ResponseEntity.ok(request);
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
}
