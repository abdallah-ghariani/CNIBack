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
                           String structure, String description, Double availability, 
                           Date updatedAt, String approvalStatus) {
        
        Api probe = new Api();
        if (name != null) probe.setName(name);
        if (secteur != null) probe.setSecteur(secteur);
        if (structure != null) probe.setStructure(structure);
        if (description != null) probe.setDescription(description);
        if (availability != null) probe.setAvailability(availability);
        if (updatedAt != null) probe.setUpdatedAt(updatedAt);
        if (approvalStatus != null) probe.setApprovalStatus(approvalStatus);
        
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();
                
        if (search != null && !search.isEmpty()) {
            matcher = matcher.withMatcher("name", match -> match.contains().ignoreCase())
                           .withMatcher("description", match -> match.contains().ignoreCase())
                           .withMatcher("secteur", match -> match.contains().ignoreCase())
                           .withMatcher("structure", match -> match.contains().ignoreCase());
            probe.setName(search);
            probe.setDescription(search);
            probe.setSecteur(search);
            probe.setStructure(search);
        }
        
        Page<Api> apiPage = apiRepository.findAll(Example.of(probe, matcher), pageable);
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
     * @return The saved API with generated ID
     */
    public Api addApi(Api api) {
        try {
            logger.info("Adding new API. Initial Approval Status: {}", api.getApprovalStatus());
            // Ensure new API gets a unique ID
            api.setId(null);
            if (api.getApprovalStatus() == null) {
                api.setApprovalStatus("pending");
                logger.info("Approval status was null, set to pending");
            }
            logger.info("Saving API with data - Name: {}, Secteur: {}, Structure: {}, Description: {}, Availability: {}, ApprovalStatus: {}, UpdatedAt: {}",
                api.getName(), api.getSecteur(), api.getStructure(), api.getDescription(), api.getAvailability(), api.getApprovalStatus(), api.getUpdatedAt());
            Api savedApi = apiRepository.save(api);
            logger.info("API saved with ID: {}, Final Approval Status: {}", savedApi.getId(), savedApi.getApprovalStatus());
            return savedApi;
        } catch (Exception e) {
            logger.error("Error saving API to repository. Error message: {}. Input data - Name: {}, Secteur: {}, Structure: {}, Description: {}, Availability: {}, ApprovalStatus: {}, UpdatedAt: {}",
                e.getMessage(), api.getName(), api.getSecteur(), api.getStructure(), api.getDescription(), api.getAvailability(), api.getApprovalStatus(), api.getUpdatedAt(), e);
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
