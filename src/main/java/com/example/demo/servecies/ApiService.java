package com.example.demo.servecies;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.demo.entity.Api;
import com.example.demo.entity.User;
import com.example.demo.repository.ApiRepository;
import com.example.demo.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    private final ApiRepository apiRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ApiService(ApiRepository apiRepository, UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.apiRepository = apiRepository;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get all APIs with optional filtering
     */
    public Page<Api> getAll(Pageable pageable, String search, String name, String secteur, 
                           String structure, String service, String description, Double availability, 
                           Date updatedAt, String approvalStatus, String sectorId, String serviceId, User user) {
        
        logger.info("Getting APIs with filters - search: {}, name: {}, secteur: {}, structure: {}, service: {}, approvalStatus: {}, sectorId: {}, serviceId: {}",
                search, name, secteur, structure, service, approvalStatus, sectorId, serviceId);
        
        // Handle structure filter first as it has highest priority
        if (structure != null && !structure.trim().isEmpty()) {
            logger.info("Filtering APIs by structure: '{}' using direct query", structure);
            
            // Get all APIs with the specified structure
            List<Api> apis = apiRepository.findByStructure(structure);
            logger.info("Found {} APIs with structure: {}", apis.size(), structure);
            
            // Apply pagination
            int start = (int)pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), apis.size());
            
            return new PageImpl<>(
                apis.subList(start, end), 
                pageable, 
                apis.size()
            );
        }
        
        // For non-structure queries, use the standard filtering approach
        Api probe = new Api();
        
        // Apply basic filters
        if (name != null && !name.trim().isEmpty()) {
            probe.setName(name);
        }
        
        if (secteur != null && !secteur.trim().isEmpty()) {
            probe.setSecteur(secteur);
        }
        
        // Set basic filters
        if (sectorId != null && !sectorId.trim().isEmpty()) {
            probe.setSecteur(sectorId.trim());
            logger.info("Filtering by sectorId: {}", sectorId);
        }
        
        if (description != null && !description.trim().isEmpty()) {
            probe.setDescription(description.trim());
        }
        
        if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
            probe.setApprovalStatus(approvalStatus.trim());
        }
        
        // Configure the matcher
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT)
                .withIgnoreCase(false)
                .withMatcher("id", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("providerId", ExampleMatcher.GenericPropertyMatchers.exact()) // For serviceId
                .withMatcher("service", ExampleMatcher.GenericPropertyMatchers.exact())     // For legacy service filter
                .withMatcher("structure", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("secteur", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("description", ExampleMatcher.GenericPropertyMatchers.contains());
        
        // Handle search term if provided
        if (search != null && !search.trim().isEmpty()) {
            probe.setName(search.trim());
            probe.setDescription(search.trim());
        }
        
        // Handle service filtering with direct query (similar to structure)
        if (serviceId != null && !serviceId.trim().isEmpty()) {
            logger.info("Filtering APIs by serviceId: {} using direct query", serviceId.trim());
            
            // Get all APIs with the specified service ID
            List<Api> apis = apiRepository.findByService(serviceId.trim());
            logger.info("Found {} APIs with service: {}", apis.size(), serviceId);
            
            // Apply pagination
            int start = (int)pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), apis.size());
            
            return new PageImpl<>(
                apis.subList(start, end), 
                pageable, 
                apis.size()
            );
        } else if (service != null && !service.trim().isEmpty()) {
            // For backward compatibility, use the service parameter
            logger.info("Filtering by service (legacy): {}", service);
            probe.setService(service.trim());
            // Clear providerId to avoid conflicts
            probe.setProviderId(null);
        }
            
        // Log the final query for debugging
        Example<Api> example = Example.of(probe, matcher);
        logger.info("Final query filter - probe: {}, matcher: {}", probe, matcher);
        
        // Debug: Log the actual query being executed
        logger.info("Executing query with parameters:");
        logger.info("- name: '{}'", probe.getName());
        logger.info("- secteur: '{}'", probe.getSecteur());
        logger.info("- service: '{}'", probe.getService());
        logger.info("- description: '{}'", probe.getDescription());
        logger.info("- approvalStatus: '{}'", probe.getApprovalStatus());
        
        // Execute the query
        logger.info("Executing database query...");
        Page<Api> apiPage = apiRepository.findAll(example, pageable);
        
        // Log the results
        logger.info("Query complete. Found {} APIs matching the filters", apiPage.getTotalElements());
        
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
    public Api deleteApi(String id) {
        Api api = getApiById(id);
        apiRepository.delete(api);
        return api;
    }
    
    /**
     * Find all approved APIs for a specific sector
     * @param sectorId The ID of the sector
     * @param pageable Pagination information
     * @return Page of approved APIs for the sector
     */
    public Page<Api> findApprovedApisBySector(String sectorId, Pageable pageable) {
        logger.info("🔍 [findApprovedApisBySector] Starting search for sector ID: {}", sectorId);
        
        try {
            // Log the pageable details
            logger.info("📋 Pageable: page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
            
            logger.info("🔎 Querying for secteur={} with approvalStatus=approved (case-insensitive)", sectorId);
            
            // Create a query with case-insensitive status check
            Query query = new Query();
            query.addCriteria(Criteria.where("secteur").is(sectorId)
                .and("approvalStatus").regex("^approved$", "i"));
            
            // Apply pagination
            query.with(pageable);
            
            // Execute the query
            List<Api> apis = mongoTemplate.find(query, Api.class);
            long total = mongoTemplate.count(query.skip(-1).limit(-1), Api.class);
            
            // Create a page with the results
            Page<Api> result = new PageImpl<>(apis, pageable, total);
            
            logger.info("✅ Query completed. Found {} APIs ({} total)", 
                result.getNumberOfElements(), result.getTotalElements());
                
            if (result.isEmpty()) {
                // Log more details when no results are found
                logger.warn("⚠️ No approved APIs found for sector ID: {}", sectorId);
                logger.info("ℹ️ Checking if any APIs exist in the database...");
                
                long totalApis = apiRepository.count();
                logger.info("ℹ️ Total APIs in database: {}", totalApis);
                
                if (totalApis > 0) {
                    // Log some sample API records to help with debugging
                    logger.info("ℹ️ Sample APIs in database (first 5):");
                    apiRepository.findAll(PageRequest.of(0, 5)).forEach(api -> 
                        logger.info("   - ID: {}, Name: {}, Secteur: {}, Status: {}", 
                            api.getId(), api.getName(), api.getSecteur(), api.getApprovalStatus()));
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("❌ Error in findApprovedApisBySector for sector {}: {}", sectorId, e.getMessage(), e);
            throw e;
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
