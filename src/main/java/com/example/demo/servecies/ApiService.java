package com.example.demo.servecies;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Api;
import com.example.demo.entity.QApi;
import com.example.demo.repository.ApiRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Service
public class ApiService {

    @Autowired
    private ApiRepository apiRepository;

    /**
     * Get all APIs with pagination, filtering, and sorting
     * 
     * @param pageable Pagination and sorting information
     * @param search Global search term (searches across name, structure, secteur, and description)
     * @param name Filter by API name
     * @param secteur Filter by sector
     * @param structure Filter by structure name
     * @param description Filter by description
     * @param availability Filter by availability percentage
     * @param updatedAt Filter by update date
     * @return Page of Api objects that match the criteria
     */
    public Page<Api> getAll(
            Pageable pageable,
            String search,
            String name,
            String secteur,
            String structure,
            String description,
            Double availability,
            Date updatedAt) {
        
        QApi qApi = new QApi("api");
        BooleanBuilder predicate = new BooleanBuilder();
        
        // Global search across all searchable fields
        if (search != null && !search.isEmpty()) {
            predicate.or(qApi.name.containsIgnoreCase(search))
                    .or(qApi.secteur.containsIgnoreCase(search))
                    .or(qApi.structure.containsIgnoreCase(search))
                    .or(qApi.description.containsIgnoreCase(search));
        }
        
        // Field-specific filters
        if (name != null && !name.isEmpty()) {
            predicate.and(qApi.name.containsIgnoreCase(name));
        }
        
        if (secteur != null && !secteur.isEmpty()) {
            predicate.and(qApi.secteur.containsIgnoreCase(secteur));
        }
        
        if (structure != null && !structure.isEmpty()) {
            predicate.and(qApi.structure.containsIgnoreCase(structure));
        }
        
        if (description != null && !description.isEmpty()) {
            predicate.and(qApi.description.containsIgnoreCase(description));
        }

        
        if (updatedAt != null) {
            predicate.and(qApi.updatedAt.eq(updatedAt));
        }
        
        if (predicate.hasValue()) {
            return apiRepository.findAll(predicate, pageable);
        }
        
        return apiRepository.findAll(pageable);
    }
    
    /**
     * Add a new API
     * 
     * @param api The API to add
     * @return The saved API with generated ID
     */
    public Api addApi(Api api) {
        // Set update date if not provided
        if (api.getUpdatedAt() == null) {
            api.setUpdatedAt(new Date());
        }
        return apiRepository.save(api);
    }
    
    /**
     * Get an API by ID
     * 
     * @param id The ID of the API to retrieve
     * @return The API if found
     * @throws ResponseStatusException if API not found
     */
    public Api getApiById(String id) {
        return apiRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "API not found with ID: " + id));
    }
    
    /**
     * Update an existing API
     * 
     * @param id The ID of the API to update
     * @param updatedApi The updated API data
     * @return The updated API
     * @throws ResponseStatusException if API not found or ID mismatch
     */
    public Api updateApi(String id, Api updatedApi) {
        if (!id.equals(updatedApi.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "API ID does not match");
        }
        
        return apiRepository.findById(id)
                .map(api -> {
                    api.setName(updatedApi.getName());
                    api.setSecteur(updatedApi.getSecteur());
                    api.setStructure(updatedApi.getStructure());
                    api.setDescription(updatedApi.getDescription());
                    api.setUpdatedAt(new Date()); // Always update the date on edit
                    return apiRepository.save(api);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "API not found with ID: " + id));
    }
    
    /**
     * Delete an API by ID
     * 
     * @param id The ID of the API to delete
     * @throws ResponseStatusException if API not found
     */
    public void deleteApi(String id) {
        if (!apiRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "API not found with ID: " + id);
        }
        apiRepository.deleteById(id);
    }
}
