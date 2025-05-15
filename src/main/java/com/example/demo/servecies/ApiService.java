package com.example.demo.servecies;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Api;
import com.example.demo.entity.QApi;
import com.example.demo.entity.User;
import com.example.demo.repository.ApiRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    @Autowired
    private ApiRepository apiRepository;

    /**
     * Get all APIs with optional filtering
     */
    public Page<Api> getAll(Pageable pageable, String search, String name, String secteur, 
                           String structure, String service, String description, Double availability, 
                           Date updatedAt, String approvalStatus, User user) {
        
        logger.info("Getting APIs with filters - search: {}, name: {}, secteur: {}, structure: {}, service: {}, approvalStatus: {}",
                search, name, secteur, structure, service, approvalStatus);
                
        QApi query = new QApi("api"); 
        
        // Two-step filtering approach for complex queries
        // First create a basic probe for exact/specific field matching
        Api probe = new Api();
        boolean hasServiceFilter = false;
        
        if (name != null) probe.setName(name);
        if (secteur != null) probe.setSecteur(secteur);
        if (structure != null) probe.setStructure(structure);
        if (service != null && !service.trim().isEmpty()) {
            logger.info("Filtering APIs by service/providerId: '{}'", service);
            probe.setProviderId(service); // Filter by providerId when service is specified
            hasServiceFilter = true;
        }
        if (description != null) probe.setDescription(description);
        if (availability != null) probe.setAvailability(availability);
        if (updatedAt != null) probe.setUpdatedAt(updatedAt);
        if (approvalStatus != null) probe.setApprovalStatus(approvalStatus);
        
        // Build the matcher with appropriate settings
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();
                
        // Add explicit matchers for each field we want to match partially
        matcher = matcher.withMatcher("name", match -> match.contains().ignoreCase())
                        .withMatcher("description", match -> match.contains().ignoreCase())
                        .withMatcher("secteur", match -> match.contains().ignoreCase())
                        .withMatcher("structure", match -> match.contains().ignoreCase());
                        
        // For providerId/service we want exact matching
        if (hasServiceFilter) {
            matcher = matcher.withMatcher("providerId", match -> match.exact());
        }
                
        // If we have a global search term, use it across multiple fields
        if (search != null && !search.isEmpty()) {
            // Here we need a different approach - we'll use the search term for multiple fields
            // but keep any service filtering intact
            Api searchProbe = new Api();
            searchProbe.setName(search);
            searchProbe.setDescription(search);
            searchProbe.setSecteur(search);
            searchProbe.setStructure(search);
            
            // If we have a service filter, maintain it
            if (hasServiceFilter) {
                searchProbe.setProviderId(service);
            }
            
            // Use the search probe instead
            probe = searchProbe;
        }
        
        // Log the final query for debugging
        Example<Api> example = Example.of(probe, matcher);
        logger.info("Final query filter - probe: {}, matcher: {}", probe, matcher);
        
        // Execute the query
        Page<Api> apiPage = apiRepository.findAll(example, pageable);
        // Set default approval status for existing APIs without this field
        apiPage.getContent().forEach(api -> {
            if (api.getApprovalStatus() == null) {
                api.setApprovalStatus("approved");
                apiRepository.save(api);
            }
        });
        return apiPage;
    }
    
    /**
     * Get an API by ID
     */
    public Api getApiById(String id) {
        Optional<Api> apiOpt = apiRepository.findById(id);
        if (apiOpt.isPresent()) {
            Api api = apiOpt.get();
            // Set default approval status for existing API without this field
            if (api.getApprovalStatus() == null) {
                api.setApprovalStatus("approved");
                apiRepository.save(api);
            }
            return api;
        } else {
            throw new RuntimeException("API not found with id: " + id);
        }
    }

    /**
     * Add a new API
     * 
     * @param api The API to add
     * @param user The current user (can be null)
     * @return The saved API with generated ID
     */
    public Api addApi(Api api, User user) {
        try {
            logger.info("Adding new API. User: {}", user != null ? user.getUsername() : "Unknown");
            
            // Ensure new API gets a unique ID
            api.setId(null);
            
            // Set updatedAt to current date
            api.setUpdatedAt(new Date());
            
            // Set the provider ID to the current user's ID
            if (user != null) {
                api.setProviderId(user.getId());
                logger.info("Setting provider ID: {} for API", user.getId());
            }
            
            // Determine if user is admin
            boolean isAdmin = false;
            if (user != null && user.getAuthorities() != null) {
                isAdmin = user.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().toLowerCase().contains("admin"));
                logger.info("User role check - Username: {}, IsAdmin: {}", user.getUsername(), isAdmin);
            }
            
            // Set approval status based on user role
            if (!isAdmin) {
                // Non-admin users (providers) always get pending status
                api.setApprovalStatus("pending");
                logger.info("Provider submitted API will require admin approval");
            } else if (api.getApprovalStatus() == null) {
                // Admin users get auto-approved if not specified
                api.setApprovalStatus("approved");
                logger.info("Admin user creating API with auto-approved status");
            } else {
                // Admin specified a status, keep it
                logger.info("Admin user creating API with specified status: {}", api.getApprovalStatus());
            }
            
            // Validate required fields
            if (api.getName() == null || api.getName().trim().isEmpty()) {
                api.setName("Unnamed API");
                logger.warn("API name was missing or empty, set to default value");
            }
            
            logger.info("Saving API with data - Name: {}, Secteur: {}, Structure: {}, Description: {}, Availability: {}, ApprovalStatus: {}",
                api.getName(), api.getSecteur(), api.getStructure(), api.getDescription(), api.getAvailability(), api.getApprovalStatus());
            
            Api savedApi = apiRepository.save(api);
            logger.info("API successfully saved with ID: {}, Final Approval Status: {}", savedApi.getId(), savedApi.getApprovalStatus());
            return savedApi;
        } catch (Exception e) {
            logger.error("Error saving API. Error message: {}. Input data - Name: {}, Secteur: {}, Structure: {}",
                e.getMessage(), api.getName(), api.getSecteur(), api.getStructure(), e);
            throw new RuntimeException("Failed to save API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update an existing API
     */
    public Api updateApi(String id, Api api) {
        Optional<Api> existingApi = apiRepository.findById(id);
        if (existingApi.isPresent()) {
            Api updatedApi = existingApi.get();
            updatedApi.setName(api.getName());
            updatedApi.setDescription(api.getDescription());
            updatedApi.setSecteur(api.getSecteur());
            updatedApi.setStructure(api.getStructure());
            updatedApi.setAvailability(api.getAvailability());
            updatedApi.setUpdatedAt(new Date());
            // Approval status is managed separately
            return apiRepository.save(updatedApi);
        } else {
            throw new RuntimeException("API not found with id: " + id);
        }
    }
    
    /**
     * Delete an API
     */
    public void deleteApi(String id) {
        if (apiRepository.existsById(id)) {
            apiRepository.deleteById(id);
        } else {
            throw new RuntimeException("API not found with id: " + id);
        }
    }
    
    /**
     * Update approval status of an API
     */
    public Api updateApprovalStatus(String id, String status) {
        Optional<Api> existingApi = apiRepository.findById(id);
        if (existingApi.isPresent()) {
            Api api = existingApi.get();
            api.setApprovalStatus(status);
            api.setUpdatedAt(new Date());
            return apiRepository.save(api);
        } else {
            throw new RuntimeException("API not found with id: " + id);
        }
    }
    
    /**
     * Get pending API requests
     */
    public Page<Api> getPendingApis(Pageable pageable) {
        Api probe = new Api();
        probe.setApprovalStatus("pending");
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues();
        return apiRepository.findAll(Example.of(probe, matcher), pageable);
    }
    
    /**
     * Get the most recent APIs for status checking
     */
    public List<Api> getRecentApis(int limit) {
        logger.info("Fetching the {} most recent APIs", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Api> apiPage = apiRepository.findAll(pageable);
        logger.info("Retrieved {} recent APIs", apiPage.getContent().size());
        return apiPage.getContent();
    }
}
