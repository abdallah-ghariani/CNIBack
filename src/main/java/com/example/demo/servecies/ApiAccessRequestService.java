package com.example.demo.servecies;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Api;
import com.example.demo.entity.ApiAccessRequest;
import com.example.demo.entity.Secteur;
import com.example.demo.entity.User;
import com.example.demo.dto.ApiAccessRequestDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ApiAccessRequestRepository;

@Service
@Transactional(readOnly = true)
public class ApiAccessRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiAccessRequestService.class);
    
    @Autowired
    private ApiAccessRequestRepository accessRequestRepository;
    
    @Autowired
    private ApiService apiService;
    
    /**
     * Create a new API access request
     */
    @Transactional
    public ApiAccessRequest createRequest(ApiAccessRequestDTO requestDTO, User requester) {
        logger.info("Creating new API access request with API ID: {}", requestDTO.getApiId());
        
        // Verify the API exists
        Api api = apiService.getApiById(requestDTO.getApiId());
        if (api == null) {
            throw new ResourceNotFoundException("API not found with id: " + requestDTO.getApiId());
        }
        
        // Create the access request
        ApiAccessRequest accessRequest = new ApiAccessRequest();
        accessRequest.setApiId(requestDTO.getApiId());
        accessRequest.setConsumerId(requester.getId());
        accessRequest.setName(requester.getUsername());
        accessRequest.setEmail(requester.getUsername()); // Using username as email
        
        // Set sector and structure names from the User entity
        if (requester.getSecteur() != null) {
            accessRequest.setSecteur(requester.getSecteur().getName());
        } else {
            accessRequest.setSecteur(null); // or set a default value if needed
        }
        
        if (requester.getStructure() != null) {
            accessRequest.setStructure(requester.getStructure().getName());
        } else {
            accessRequest.setStructure(null); // or set a default value if needed
        }
        
        accessRequest.setMessage(requestDTO.getMessage());
        accessRequest.setApiName(api.getName());
        
        // Set the provider ID from the API
        accessRequest.setProviderId(api.getProviderId());
        accessRequest.setApiSector(api.getSecteur());
        accessRequest.setRequesterSector(requester.getSecteur() != null ? requester.getSecteur().getName() : null);
        accessRequest.setStatus("PENDING");
        
        // Save the request
        return accessRequestRepository.save(accessRequest);
    }
    
    /**
     * Find API access requests by API sector with pagination and optional status filter
     */
    public Page<ApiAccessRequest> findBySector(Secteur secteur, String status, Pageable pageable) {
        logger.info("Finding API access requests for sector: {}", secteur.getName());
        if (status != null && !status.isEmpty()) {
            return accessRequestRepository.findByApiSectorAndStatus(
                secteur.getName(),
                status.toUpperCase(),
                pageable
            );
        } else {
            return accessRequestRepository.findByApiSector(secteur.getName(), pageable);
        }
    }
    
    /**
     * Find API access requests by requester with optional status filter
     */
    public Page<ApiAccessRequest> findByRequester(User requester, String status, Pageable pageable) {
        logger.info("Finding API access requests for requester: {}", requester.getId());
        if (status != null && !status.isEmpty()) {
            return accessRequestRepository.findByConsumerIdAndStatus(
                requester.getId(),
                status.toUpperCase(),
                pageable
            );
        } else {
            return accessRequestRepository.findByConsumerId(requester.getId(), pageable);
        }
    }
    
    /**
     * Check if a user can manage (approve/reject) a request
     */
    public boolean canManageRequest(User user, String requestId) {
        ApiAccessRequest request = getRequestById(requestId);
        return user.getSecteur() != null && 
               user.getSecteur().getName().equals(request.getApiSector());
    }
    
    /**
     * Check if a user can view a request
     */
    public boolean canViewRequest(User user, ApiAccessRequest request) {
        // User can view if they are the requester or from the API's sector
        return user.getId().equals(request.getConsumerId()) || 
               (user.getSecteur() != null && 
                user.getSecteur().getName().equals(request.getApiSector()));
    }
    
    /**
     * Check if an API is in the user's sector
     */
    public boolean isApiInUserSector(User user, String apiId) {
        Api api = apiService.getApiById(apiId);
        return user.getSecteur() != null && 
               user.getSecteur().getName().equals(api.getSecteur());
    }
    
    /**
     * Get all API access requests made by the authenticated consumer
     */
    public List<ApiAccessRequest> getRequestsForConsumer(String consumerId) {
        return accessRequestRepository.findByConsumerId(consumerId);
    }
    
    /**
     * Get all API access requests for the authenticated provider's APIs
     */
    public List<ApiAccessRequest> getRequestsForProvider(String providerId) {
        return accessRequestRepository.findByProviderId(providerId);
    }
    
    /**
     * Get API access request by ID
     */
    public Optional<ApiAccessRequest> findById(String requestId) {
        return accessRequestRepository.findById(requestId);
    }
    
    /**
     * Get a specific API access request
     */
    public ApiAccessRequest getRequestById(String requestId) {
        return accessRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("API access request not found with ID: " + requestId));
    }
    
    /**
     * Approve an API access request
     * Only users from the API's sector can approve a request
     */
    @Transactional
    public ApiAccessRequest approveRequest(String requestId, User approver, String feedback) {
        ApiAccessRequest request = getRequestById(requestId);
        
        // Verify approver is from the API's sector
        if (approver.getSecteur() == null || 
            !approver.getSecteur().getName().equals(request.getApiSector())) {
            throw new AccessDeniedException("Not authorized to approve this request");
        }
        
        // Update request status
        request.setStatus("APPROVED");
        request.setApprovedById(approver.getId());
        request.setApprovalDate(LocalDateTime.now());
        request.setFeedback(feedback);
        
        // Grant API access to the requester (implement this in ApiService if needed)
        // apiService.grantAccess(request.getApiId(), request.getConsumerId());
        
        // Send notification to requester (uncomment when email service is implemented)
        /*
        if (emailService != null) {
            emailService.sendRequestApprovalNotification(
                request.getEmail(),
                request.getApiName(),
                feedback
            );
        }
        */
        
        return accessRequestRepository.save(request);
    }
    
    /**
     * Reject an API access request
     * Only users from the API's sector can reject a request
     */
    @Transactional
    public ApiAccessRequest rejectRequest(String requestId, User rejector, String feedback) {
        ApiAccessRequest request = getRequestById(requestId);
        
        // Verify rejector is from the API's sector
        if (rejector.getSecteur() == null || 
            !rejector.getSecteur().getName().equals(request.getApiSector())) {
            throw new AccessDeniedException("Not authorized to reject this request");
        }
        
        // Update request status
        request.setStatus("REJECTED");
        request.setRejectedById(rejector.getId());
        request.setRejectionDate(LocalDateTime.now());
        request.setFeedback(feedback);
        
        // Send notification to requester (uncomment when email service is implemented)
        /*
        if (emailService != null) {
            emailService.sendRequestRejectionNotification(
                request.getEmail(),
                request.getApiName(),
                feedback
            );
        }
        */
        
        return accessRequestRepository.save(request);
    }
}
