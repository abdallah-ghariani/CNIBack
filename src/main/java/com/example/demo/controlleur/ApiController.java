package com.example.demo.controlleur;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Api;
import com.example.demo.servecies.ApiService;

@RestController
@RequestMapping("/api/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ApiService apiService;
    
    /**
     * Get all APIs with pagination, search and sorting.
     * 
     * Pagination parameters:
     * - page: Page number (zero-based)
     * - size: Number of items per page
     * 
     * Search parameters:
     * - search: Global search term (searches across all searchable fields)
     * - name: Filter by API name
     * - secteur: Filter by sector
     * - structure: Filter by structure name
     * - description: Filter by description
     * - availability: Filter by availability percentage
     * - updatedAt: Filter by update date
     * - approvalStatus: Filter by approval status (pending, approved, rejected)
     * 
     * Sorting parameters:
     * - sort: Format is {fieldName},{direction} (e.g., "name,asc" or "updatedAt,desc")
     */
    @GetMapping
    public ResponseEntity<Page<Api>> getAllApis(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String secteur,
            @RequestParam(required = false) String structure,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double availability,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date updatedAt) {
        logger.info("Retrieving all APIs, search: {}, name: {}, secteur: {}, structure: {}, description: {}, availability: {}, approvalStatus: {}, updatedAt: {}", 
            search, name, secteur, structure, description, availability, approvalStatus, updatedAt);
        Page<Api> apiPage = apiService.getAll(pageable, search, name, secteur, structure, description, availability, updatedAt, approvalStatus);
        logger.info("Retrieved {} APIs", apiPage.getContent().size());
        for (Api api : apiPage.getContent()) {
            logger.info("API ID: {}, Name: {}, Approval Status: {}", api.getId(), api.getName(), api.getApprovalStatus());
        }
        return new ResponseEntity<>(apiPage, HttpStatus.OK);
    }
    
    /**
     * Get an API by ID
     */
    @GetMapping("/{id}")
    public Api getApiById(@PathVariable String id) {
        return apiService.getApiById(id);
    }
    
    /**
     * Create a new API - for providers it sets status to pending
     */
    @PostMapping
    public ResponseEntity<Api> createApi(@RequestBody Api api, @AuthenticationPrincipal User user) {
        try {
            logger.info("Creating new API. User: {}, Roles: {}", user != null ? user.getUsername() : "Unknown", user != null ? user.getAuthorities() : "No user");
            // Log all authorities explicitly for debugging
            StringBuilder authoritiesLog = new StringBuilder("User authorities: ");
            if (user != null && user.getAuthorities() != null) {
                user.getAuthorities().forEach(auth -> authoritiesLog.append(auth.getAuthority()).append(", "));
            } else {
                authoritiesLog.append("No authorities available");
            }
            logger.info(authoritiesLog.toString());
            // Additional authentication details for debugging using Authentication object
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                logger.info("Authentication details - IsAuthenticated: {}, Authorities: {}, Details: {}",
                    authentication.isAuthenticated(), authentication.getAuthorities(), authentication.getDetails());
            } else {
                logger.warn("No authentication object found in SecurityContext");
            }
            
            // Log input API data for debugging
            logger.info("Input API data - Name: {}, Secteur: {}, Structure: {}, Description: {}, Availability: {}, ApprovalStatus: {}, UpdatedAt: {}",
                api.getName(), api.getSecteur(), api.getStructure(), api.getDescription(), api.getAvailability(), api.getApprovalStatus(), api.getUpdatedAt());
            
            // Ensure minimal data for API creation to avoid validation failures
            if (api.getName() == null || api.getName().trim().isEmpty()) {
                api.setName("Unnamed API");
                logger.warn("API name was missing or empty, set to default value");
            }
            // Set other required fields if missing
            if (api.getUpdatedAt() == null) {
                api.setUpdatedAt(new Date());
                logger.info("UpdatedAt was missing, set to current date");
            }
            // Force pending status
            api.setApprovalStatus("pending");
            logger.info("Forcing API status to pending for all users as a temporary workaround");
            
            // Check if user has admin role explicitly - very strict check (though not used due to forced pending status)
            boolean isAdmin = false;
            if (user != null && user.getAuthorities() != null) {
                isAdmin = user.getAuthorities().stream()
                    .anyMatch(authority -> {
                        String auth = authority.getAuthority().toLowerCase();
                        boolean match = auth.contains("admin");
                        logger.info("Checking for admin authority: {}, IsAdmin: {}", auth, match);
                        return match;
                    });
            }
            
            // Detailed logging for user roles
            logger.info("User roles: {}", user.getAuthorities());
            logger.info("Is user admin? {}", isAdmin);
            
            // Commented out original logic for reference
            // If not explicitly admin, treat as provider and set to pending
            // if (!isAdmin) {
            //     api.setApprovalStatus("pending");
            //     logger.info("User is NOT identified as admin (no admin in roles), setting API status to pending");
            // } else {
            //     api.setApprovalStatus("approved");
            //     logger.info("User is identified as admin, setting API status to approved");
            // }
            
            Api createdApi = apiService.addApi(api);
            logger.info("API created with ID: {}, Status: {}", createdApi.getId(), createdApi.getApprovalStatus());
            return new ResponseEntity<>(createdApi, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating API for user {}. Error message: {}. Input data - Name: {}, Secteur: {}, Structure: {}, Description: {}, Availability: {}, ApprovalStatus: {}, UpdatedAt: {}",
                user != null ? user.getUsername() : "Unknown", e.getMessage(), api.getName(), api.getSecteur(), api.getStructure(), api.getDescription(), api.getAvailability(), api.getApprovalStatus(), api.getUpdatedAt(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update an existing API
     */
    @PutMapping("/{id}")
    public ResponseEntity<Api> updateApi(@PathVariable String id, @RequestBody Api api, @AuthenticationPrincipal User user) {
        logger.info("Updating API with ID: {}", id);
        Api existingApi = apiService.getApiById(id);
        
        // Prevent non-admin users from changing approval status
        boolean isAdmin = user != null && user.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().toLowerCase().contains("admin"));
        if (!isAdmin) {
            api.setApprovalStatus(existingApi.getApprovalStatus());
            logger.info("Non-admin user attempting to update API, preserving original approval status: {}", existingApi.getApprovalStatus());
        }
        
        Api updatedApi = apiService.updateApi(id, api);
        return new ResponseEntity<>(updatedApi, HttpStatus.OK);
    }
    
    /**
     * Delete an API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApi(@PathVariable String id) {
        apiService.deleteApi(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * Approve an API - Admin only
     */
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Api> approveApi(@PathVariable String id) {
        Api approvedApi = apiService.updateApprovalStatus(id, "approved");
        return new ResponseEntity<>(approvedApi, HttpStatus.OK);
    }
    
    /**
     * Reject an API - Admin only
     */
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<Api> rejectApi(@PathVariable String id) {
        Api rejectedApi = apiService.updateApprovalStatus(id, "rejected");
        return new ResponseEntity<>(rejectedApi, HttpStatus.OK);
    }
    
    /**
     * Get pending API requests - Admin only
     */
    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/pending")
    public Page<Api> getPendingApis(Pageable pageable) {
        return apiService.getPendingApis(pageable);
    }
    
    /**
     * Get the approval status of the most recent APIs for debugging purposes
     */
    @GetMapping("/recent-status")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Api>> getRecentApiStatus() {
        logger.info("Retrieving approval status of recent APIs");
        List<Api> recentApis = apiService.getRecentApis(5); // Get the 5 most recent APIs
        logger.info("Retrieved {} recent APIs", recentApis.size());
        for (Api api : recentApis) {
            logger.info("Recent API ID: {}, Name: {}, Approval Status: {}", api.getId(), api.getName(), api.getApprovalStatus());
        }
        return new ResponseEntity<>(recentApis, HttpStatus.OK);
    }
}
