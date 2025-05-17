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
import com.example.demo.entity.User;
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
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double availability,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date updatedAt,@AuthenticationPrincipal User user ) {
        logger.info("Retrieving all APIs, search: {}, name: {}, secteur: {}, structure: {}, service: {}, description: {}, availability: {}, approvalStatus: {}, updatedAt: {}", 
            search, name, secteur, structure, service, description, availability, approvalStatus, updatedAt);
        Page<Api> apiPage = apiService.getAll(pageable, search, name, secteur, structure, service, description, availability, updatedAt, approvalStatus, user);
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
     * Get an API by code (ID)
     * This endpoint allows retrieving an API using the 'code/{id}' path format
     */
    @GetMapping("/code/{id}")
    public Api getApiByCode(@PathVariable String id) {
        logger.info("Retrieving API by code: {}", id);
        return apiService.getApiById(id);
    }
    
    /**
     * Direct API creation has been removed.
     * APIs should now be created through the request process using:
     * - /api/api-access-request/new-api
     * - /api/api-request/new-api
     */
    
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
     * NOTE: API approval/rejection endpoints have been moved to ApiRequestController
     * APIs are now approved or rejected through the request process at:
     * - PUT /api/api-request/{requestId}/approve
     * - PUT /api/api-request/{requestId}/reject
     */
    
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
