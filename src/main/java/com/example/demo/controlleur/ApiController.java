package com.example.demo.controlleur;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * 
     * Sorting parameters:
     * - sort: Format is {fieldName},{direction} (e.g., "name,asc" or "updatedAt,desc")
     */
    @GetMapping
    public Page<Api> getAllApis(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String secteur,
            @RequestParam(required = false) String structure,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double availability,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date updatedAt) {
        
        return apiService.getAll(pageable, search, name, secteur, 
                             structure, description, availability, updatedAt);
    }
    
    /**
     * Get an API by ID
     */
    @GetMapping("/{id}")
    public Api getApiById(@PathVariable String id) {
        return apiService.getApiById(id);
    }
    
    /**
     * Create a new API
     */
    @PostMapping
    public ResponseEntity<Api> createApi(@RequestBody Api api) {
        Api createdApi = apiService.addApi(api);
        return new ResponseEntity<>(createdApi, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing API
     */
    @PutMapping("/{id}")
    public Api updateApi(@PathVariable String id, @RequestBody Api api) {
        return apiService.updateApi(id, api);
    }
    
    /**
     * Delete an API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApi(@PathVariable String id) {
        apiService.deleteApi(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
