package com.example.demo.controlleur;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.example.demo.entity.ApiRequest;
import com.example.demo.entity.User;
import com.example.demo.entity.ApiAccessRequest;
import com.example.demo.dto.ApiAccessRequestDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedAccessException;
import com.example.demo.servecies.ApiAccessRequestService;
import com.example.demo.servecies.UserService;

/**
 * Controller for managing API access requests between different sectors
 */
@RestController
@RequestMapping("/api/api-request")
@Validated
public class ApiAccessRequestController {

    private static final String DEFAULT_PAGE_SIZE = "10";
    private static final String DEFAULT_PAGE_NUMBER = "0";

    @Autowired
    private ApiAccessRequestService requestService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Submit a new API access request for an API from another sector
     * @param requestDTO API request details
     * @param userDetails authenticated user details
     * @return created API request
     */
    @GetMapping("/sector")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ApiAccessRequest>> getRequestsForSector(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) String status) {
        
        User currentUser = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if user has a sector assigned
        if (currentUser.getSecteur() == null) {
            throw new UnauthorizedAccessException("User does not belong to any sector");
        }
        
        Page<ApiAccessRequest> requests = requestService.findBySector(
            currentUser.getSecteur(),
            status,
            PageRequest.of(page, size, Sort.by("requestDate").descending())
        );
        
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get all API requests made by the current user
     * These are requests the current user has made to access APIs in other sectors
     * @param userDetails authenticated user details
     * @param page page number (0-based)
     * @param size page size
     * @param status filter by status (optional)
     * @return paginated list of API requests
     */
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ApiAccessRequest>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) String status) {
        
        User currentUser = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validate user has a sector
        if (currentUser.getSecteur() == null) {
            throw new UnauthorizedAccessException("User does not belong to any sector");
        }
        
        Page<ApiAccessRequest> requests = requestService.findByRequester(
            currentUser,
            status,
            PageRequest.of(page, size, Sort.by("requestDate").descending())
        );
        
        return ResponseEntity.ok(requests);
    }
    

    /**
     * Approve an API access request
     * @param requestId ID of the request to approve
     * @param feedback optional feedback or comments
     * @param userDetails authenticated user details
     * @return the approved request
     */
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiAccessRequest> approveRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User approver = userService.findById(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the approver has the right to approve this request
        if (!requestService.canManageRequest(approver, requestId)) {
            throw new UnauthorizedAccessException("You are not authorized to approve this request");
        }
        
        ApiAccessRequest approvedRequest = requestService.approveRequest(
            requestId, 
            approver,
            feedback
        );
        
        return ResponseEntity.ok(approvedRequest);
    }

    /**
     * Reject an API access request
     * @param requestId ID of the request to reject
     * @param feedback optional feedback or reason for rejection
     * @param userDetails authenticated user details
     * @return the rejected request
     */
    @PutMapping("/{requestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiAccessRequest> rejectRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User rejector = userService.findById(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the rejector has the right to reject this request
        if (!requestService.canManageRequest(rejector, requestId)) {
            throw new UnauthorizedAccessException("You are not authorized to reject this request");
        }
        
        ApiAccessRequest rejectedRequest = requestService.rejectRequest(
            requestId, 
            rejector,
            feedback
        );
        
        return ResponseEntity.ok(rejectedRequest);
    }
    
    /**
     * Get details of a specific API request
     * @param requestId ID of the request to retrieve
     * @param userDetails authenticated user details
     * @return the requested API request
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiAccessRequest> getRequestDetails(
            @PathVariable String requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        ApiAccessRequest request = requestService.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("API request not found with ID: " + requestId));
            
        // Verify the user has permission to view this request
        // Either they are the requester or they are in the target sector
        if (!requestService.canViewRequest(user, request)) {
            throw new UnauthorizedAccessException("You are not authorized to view this request");
        }
        
        return ResponseEntity.ok(request);
    }
}