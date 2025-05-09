package com.example.demo.controlleur;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Service;
import com.example.demo.servecies.ServiceService;


@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    /**
     * Get all services with pagination
     */
    @GetMapping
    public Page<Service> getAllServices(Pageable pageable) {
        return serviceService.getAll(pageable);
    }
    
    /**
     * Get a service by ID
     */
    @GetMapping("/{id}")
    public Service getServiceById(@PathVariable String id) {
        return serviceService.getServiceById(id);    
    }
    
    /**
     * Create a new service
     */
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    public Service addService(@RequestBody Service service) {
        return serviceService.createService(service);
    }

    /**
     * Update an existing service
     */
    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(@PathVariable String id, @RequestBody Service service) {
        try {
            return ResponseEntity.ok(serviceService.updateService(id, service));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a service
     */
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        try {
            serviceService.deleteService(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Add an API to a service
     */
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/{serviceId}/apis/{apiId}")
    public ResponseEntity<Service> addApiToService(
            @PathVariable String serviceId,
            @PathVariable String apiId) {
        try {
            return ResponseEntity.ok(serviceService.addApiToService(serviceId, apiId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Remove an API from a service
     */
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{serviceId}/apis/{apiId}")
    public ResponseEntity<Service> removeApiFromService(
            @PathVariable String serviceId,
            @PathVariable String apiId) {
        try {
            return ResponseEntity.ok(serviceService.removeApiFromService(serviceId, apiId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
