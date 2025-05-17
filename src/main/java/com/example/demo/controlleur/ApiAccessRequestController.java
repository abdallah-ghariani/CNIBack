package com.example.demo.controlleur;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.ApiAccessRequestDTO;
import com.example.demo.entity.ApiRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.servecies.ApiAccessRequestService;
import com.example.demo.servecies.UserService;

@RestController
@RequestMapping("/api/api-access-request")
public class ApiAccessRequestController {

    @Autowired
    private ApiAccessRequestService requestService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<?> requestApiAccess(
            @RequestBody ApiAccessRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Get the authenticated user by ID
            String userId = ((com.example.demo.entity.User) userDetails).getId();
            User requester = userService.getUserById(userId);
            
            // Create the API access request
            ApiRequest request = requestService.createRequest(requestDTO, requester);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Error creating API access request: " + e.getMessage());
        }
    }
    
    @GetMapping("/for-my-apis")
    public ResponseEntity<?> getRequestsForMyApis(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Get the authenticated user by ID
            String userId = ((com.example.demo.entity.User) userDetails).getId();
            User currentUser = userService.getUserById(userId);
                
            // Check if user has a sector assigned
            if (currentUser.getSecteur() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Access denied: User does not have a sector assigned");
            }
            
            // Get paginated requests for the user's sector
            Page<ApiRequest> requests = requestService.findByApiSector(
                currentUser.getSecteur().getName(), 
                PageRequest.of(page, size, Sort.by("requestDate").descending())
            );
            
            return ResponseEntity.ok(requests);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Error retrieving API requests: " + e.getMessage());
        }
    }
    

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Get the authenticated user by ID
            String userId = ((com.example.demo.entity.User) userDetails).getId();
            User approver = userService.getUserById(userId);
                
            // Check if user has a sector assigned
            if (approver.getSecteur() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Access denied: User does not have a sector assigned");
            }
            
            // Approve the request
            ApiRequest approvedRequest = requestService.approveRequest(
                requestId, 
                approver,
                feedback
            );
            
            return ResponseEntity.ok(approvedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Error approving request: " + e.getMessage());
        }
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Get the authenticated user by ID
            String userId = ((com.example.demo.entity.User) userDetails).getId();
            User rejector = userService.getUserById(userId);
                
            // Check if user has a sector assigned
            if (rejector.getSecteur() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Access denied: User does not have a sector assigned");
            }
            
            // Reject the request
            ApiRequest rejectedRequest = requestService.rejectRequest(
                requestId, 
                rejector,
                feedback
            );
            
            return ResponseEntity.ok(rejectedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Error rejecting request: " + e.getMessage());
        }
    }
}
