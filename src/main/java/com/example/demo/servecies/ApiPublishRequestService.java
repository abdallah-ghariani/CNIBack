package com.example.demo.servecies;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Api;
import com.example.demo.entity.ApiPublishRequest;
import com.example.demo.entity.User;
import com.example.demo.dto.ApiPublishRequestDTO;
import com.example.demo.repository.ApiPublishRequestRepository;

@Service
public class ApiPublishRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiPublishRequestService.class);
    
    @Autowired
    private ApiPublishRequestRepository publishRequestRepository;
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new API publish request
     */
    public ApiPublishRequest createPublishRequest(ApiPublishRequestDTO requestDTO) {
        logger.info("Creating new API publish request with API ID: {}", requestDTO.getApiId());
        
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        // Verify the API exists and belongs to the current provider
        Api api = apiService.getApiById(requestDTO.getApiId());
        if (!api.getProviderId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You can only submit publish requests for your own APIs");
        }
        
        ApiPublishRequest publishRequest = new ApiPublishRequest(
            requestDTO.getApiId(),
            currentUser.getId(),
            currentUser.getUsername(),
            api.getName()
        );
        
        publishRequest.setDescription(requestDTO.getDescription());
        publishRequest.setService(requestDTO.getService());
        publishRequest.setMetadata(requestDTO.getMetadata());
        
        return publishRequestRepository.save(publishRequest);
    }
    
    /**
     * Get all API publish requests made by the authenticated provider
     */
    public List<ApiPublishRequest> getRequestsForProvider() {
        User currentUser = userService.getCurrentUser();
        return publishRequestRepository.findByProviderId(currentUser.getId());
    }
    
    /**
     * Get all API publish requests (admin only)
     */
    public List<ApiPublishRequest> getAllRequests() {
        return publishRequestRepository.findAll();
    }
    
    /**
     * Get pending API publish requests (admin only)
     */
    public List<ApiPublishRequest> getPendingRequests() {
        return publishRequestRepository.findByStatus("pending");
    }
    
    /**
     * Get a specific API publish request
     */
    public ApiPublishRequest getRequestById(String requestId) {
        return publishRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("API publish request not found with ID: " + requestId));
    }
    
    /**
     * Approve an API publish request (admin only)
     * This also sets the API's approval status to approved
     */
    public ApiPublishRequest approveRequest(String requestId, String feedback) {
        ApiPublishRequest request = getRequestById(requestId);
        request.setStatus("approved");
        request.setAdminFeedback(feedback);
        
        // Update the API's status as well
        apiService.updateApprovalStatus(request.getApiId(), "approved");
        
        return publishRequestRepository.save(request);
    }
    
    /**
     * Reject an API publish request (admin only)
     * This also sets the API's approval status to rejected
     */
    public ApiPublishRequest rejectRequest(String requestId, String feedback) {
        ApiPublishRequest request = getRequestById(requestId);
        request.setStatus("rejected");
        request.setAdminFeedback(feedback);
        
        // Update the API's status as well
        apiService.updateApprovalStatus(request.getApiId(), "rejected");
        
        return publishRequestRepository.save(request);
    }
}
