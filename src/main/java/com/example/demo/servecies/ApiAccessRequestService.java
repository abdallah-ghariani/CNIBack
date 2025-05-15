package com.example.demo.servecies;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Api;
import com.example.demo.entity.ApiAccessRequest;
import com.example.demo.entity.User;
import com.example.demo.dto.ApiAccessRequestDTO;
import com.example.demo.dto.UserCredentialsDto;
import com.example.demo.repository.ApiAccessRequestRepository;

@Service
public class ApiAccessRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiAccessRequestService.class);
    
    @Autowired
    private ApiAccessRequestRepository accessRequestRepository;
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new API access request
     */
    public ApiAccessRequest createRequest(ApiAccessRequestDTO requestDTO) {
        logger.info("Creating new API access request with API ID: {}", requestDTO.getApiId());
        
        User currentUser = userService.getCurrentUser();
        String consumerId = currentUser != null ? currentUser.getId() : null;
        
        // Verify the API exists
        Api api = apiService.getApiById(requestDTO.getApiId());
        
        ApiAccessRequest accessRequest = new ApiAccessRequest(
            requestDTO.getApiId(),
            consumerId,
            requestDTO.getName(),
            requestDTO.getEmail(),
            requestDTO.getSecteur(),
            requestDTO.getStructure(),
            requestDTO.getMessage(),
            api.getName()
        );
        
        // Set the provider ID from the API
        accessRequest.setProviderId(api.getProviderId());
        
        // Set additional fields if provided
        accessRequest.setService(requestDTO.getService());
        accessRequest.setDescription(requestDTO.getDescription());
        accessRequest.setMetadata(requestDTO.getMetadata());
        
        return accessRequestRepository.save(accessRequest);
    }
    
    /**
     * Get all API access requests made by the authenticated consumer
     */
    public List<ApiAccessRequest> getRequestsForConsumer() {
        User currentUser = userService.getCurrentUser();
        return accessRequestRepository.findByConsumerId(currentUser.getId());
    }
    
    /**
     * Get all API access requests for the authenticated provider's APIs
     */
    public List<ApiAccessRequest> getRequestsForProvider() {
        User currentUser = userService.getCurrentUser();
        return accessRequestRepository.findByProviderId(currentUser.getId());
    }
    
    /**
     * Get all API access requests (admin only)
     */
    public List<ApiAccessRequest> getAllRequests() {
        return accessRequestRepository.findAll();
    }
    
    /**
     * Get a specific API access request
     */
    public ApiAccessRequest getRequestById(String requestId) {
        return accessRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("API access request not found with ID: " + requestId));
    }
    
    /**
     * Approve an API access request
     * Only the provider of the API can approve a request
     */
    public ApiAccessRequest approveRequest(String requestId) {
        ApiAccessRequest request = getRequestById(requestId);
        
        // Check if current user is the provider of the API
        User currentUser = userService.getCurrentUser();
        if (!request.getProviderId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only the provider of the API can approve access requests");
        }
        
        // Update request status
        request.setStatus("approved");
        return accessRequestRepository.save(request);
    }
    
    /**
     * Reject an API access request
     * Only the provider of the API can reject a request
     */
    public ApiAccessRequest rejectRequest(String requestId) {
        ApiAccessRequest request = getRequestById(requestId);
        
        // Check if current user is the provider of the API
        User currentUser = userService.getCurrentUser();
        if (!request.getProviderId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only the provider of the API can reject access requests");
        }
        
        request.setStatus("rejected");
        return accessRequestRepository.save(request);
    }
}
