package com.example.demo.servecies;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Api;
import com.example.demo.entity.ApiAccessRequest;
import com.example.demo.entity.ApiRequest;
import com.example.demo.entity.User;
import com.example.demo.dto.ApiAccessRequestDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ApiAccessRequestRepository;
import com.example.demo.repository.ApiRequestRepository;

@Service
public class ApiAccessRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiAccessRequestService.class);
    
    @Autowired
    private ApiAccessRequestRepository accessRequestRepository;
    
    @Autowired
    private ApiRequestRepository apiRequestRepository;
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Create a new API access request
     */
    @Transactional
    public ApiRequest createRequest(ApiAccessRequestDTO requestDTO, User requester) {
        logger.info("Creating new API access request for API ID: {} by user: {}", 
                   requestDTO.getApiId(), requester.getUsername());
        
        // Verify the API exists
        Api api = apiService.getApiById(requestDTO.getApiId());
        
        // Create the API request
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setApiId(api.getId());
        apiRequest.setConsumerId(requester.getId());
        apiRequest.setProviderId(api.getProviderId());
        apiRequest.setName(requester.getUsername());
        apiRequest.setEmail(requester.getEmail());
        apiRequest.setSecteur(requester.getSecteur() != null ? requester.getSecteur().getName() : null);
        apiRequest.setStructure(requester.getStructure() != null ? requester.getStructure().getName() : null);
        apiRequest.setMessage(requestDTO.getMessage());
        apiRequest.setRequestDate(new java.util.Date());
        apiRequest.setStatus("pending");
        apiRequest.setApiName(api.getName());
        apiRequest.setService(requestDTO.getService());
        apiRequest.setDescription(requestDTO.getDescription());
        apiRequest.setMetadata(requestDTO.getMetadata());
        
        // Save the request
        return apiRequestRepository.save(apiRequest);
    }
    
    /**
     * Find API requests by API sector with pagination
     */
    @Transactional(readOnly = true)
    public Page<ApiRequest> findByApiSector(String secteur, Pageable pageable) {
        logger.info("Finding API requests for sector: {}", secteur);
        return apiRequestRepository.findBySecteurAndStatus(
            secteur, 
            "pending",
            pageable
        );
    }
    
    /**
     * Get all API access requests made by the authenticated consumer
     */
    @Transactional(readOnly = true)
    public List<ApiRequest> getRequestsForConsumer(User user) {
        return apiRequestRepository.findByConsumerId(user.getId());
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
     * Only users from the API's sector can approve the request
     */
    @Transactional
    public ApiRequest approveRequest(String requestId, User approver, String feedback) {
        ApiRequest request = apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        
        // Verify approver is from the API's sector
        if (!approver.getSecteur().getName().equals(request.getSecteur())) {
            throw new AccessDeniedException("Not authorized to approve this request");
        }
        
        // Update request status
        request.setStatus("approved");
        request.setApprovedBy(approver.getUsername());
        request.setApprovalDate(LocalDateTime.now());
        request.setFeedback(feedback);
        
        // Send notification to requester
        emailService.sendRequestApprovalNotification(
            request.getEmail(),
            request.getApiName(),
            feedback
        );
        
        return apiRequestRepository.save(request);
    }
    
    /**
     * Reject an API access request
     * Only users from the API's sector can reject the request
     */
    @Transactional
    public ApiRequest rejectRequest(String requestId, User rejector, String feedback) {
        ApiRequest request = apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        
        // Verify rejector is from the API's sector
        if (!rejector.getSecteur().getName().equals(request.getSecteur())) {
            throw new AccessDeniedException("Not authorized to reject this request");
        }
        
        // Update request status
        request.setStatus("rejected");
        request.setApprovedBy(rejector.getUsername());
        request.setApprovalDate(LocalDateTime.now());
        request.setFeedback(feedback);
        
        // Send notification to requester
        emailService.sendRequestRejectionNotification(
            request.getEmail(),
            request.getApiName(),
            feedback
        );
        
        return apiRequestRepository.save(request);
    }
    
    /**
     * Get a specific API request by ID
     */
    @Transactional(readOnly = true)
    public ApiRequest getRequestById(String requestId) {
        return apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("API request not found with ID: " + requestId));
    }
}
