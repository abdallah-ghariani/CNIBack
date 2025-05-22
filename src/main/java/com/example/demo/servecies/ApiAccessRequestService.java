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
        
        // Use either the sector ID or name based on how it's stored in the database
        // Check if apiSector field in database contains IDs or names
        String sectorIdentifier = secteur.getId(); // Use ID as the default
        
        // If your data shows apiSector is actually storing the name, use this instead:
        // String sectorIdentifier = secteur.getName();
        
        logger.info("Using sector identifier: {}", sectorIdentifier);
        
        if (status != null && !status.isEmpty()) {
            return accessRequestRepository.findByApiSectorAndStatus(
                sectorIdentifier,
                status.toUpperCase(),
                pageable
            );
        } else {
            return accessRequestRepository.findByApiSector(sectorIdentifier, pageable);
        }
    }
    
    /**
     * Find API access requests by requester with optional status filter
     */
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
     * Find API access requests by requester with a specific status
     * @param requester The user who made the requests
     * @param status The status to filter by (e.g., "APPROVED", "PENDING", "REJECTED")
     * @param pageable Pagination information
     * @return Page of API access requests matching the criteria
     */
    public Page<ApiAccessRequest> findByRequesterAndStatus(User requester, String status, Pageable pageable) {
        logger.info("Finding {} API access requests for requester: {}", status, requester.getId());
        return accessRequestRepository.findByConsumerIdAndStatus(
            requester.getId(),
            status.toUpperCase(),
            pageable
        );
    }
    
    /**
     * Check if a user can manage (approve/reject) a request
     */
    public boolean canManageRequest(User user, String requestId) {
        ApiAccessRequest request = getRequestById(requestId);
        
        if (user.getSecteur() == null) {
            logger.warn("User {} has no sector assigned", user.getId());
            return false;
        }
        
        String userSectorId = user.getSecteur().getId();
        logger.info("Checking if user sector ID: {} matches API sector: {} for request {}", 
            userSectorId, request.getApiSector(), requestId);
            
        return userSectorId.equals(request.getApiSector());
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
        if (approver.getSecteur() == null) {
            throw new AccessDeniedException("Not authorized to approve this request: user has no sector");
        }
        
        // Get the sector ID from the approver's sector
        String approverSectorId = approver.getSecteur().getId();
        
        // Compare with the API sector ID stored in the request
        logger.info("Comparing approver sector ID: {} with request API sector: {}", approverSectorId, request.getApiSector());
        
        if (!approverSectorId.equals(request.getApiSector())) {
            throw new AccessDeniedException("Not authorized to approve this request: sector mismatch");
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
        if (rejector.getSecteur() == null) {
            throw new AccessDeniedException("Not authorized to reject this request: user has no sector");
        }
        
        // Get the sector ID from the rejector's sector
        String rejectorSectorId = rejector.getSecteur().getId();
        
        // Compare with the API sector ID stored in the request
        logger.info("Comparing rejector sector ID: {} with request API sector: {}", rejectorSectorId, request.getApiSector());
        
        if (!rejectorSectorId.equals(request.getApiSector())) {
            throw new AccessDeniedException("Not authorized to reject this request: sector mismatch");
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
