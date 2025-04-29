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

    @GetMapping
    public Page<Service> getAllServices(Pageable pageable) {
        return serviceService.getAll(pageable);
    }
    
    @GetMapping("/{id}")
    public Service getServiceById(@PathVariable String id) {
        return  serviceService.getServiceById(id);    
    }
    
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    public Service addService(@RequestBody Service service) {
        return serviceService.addService(service.getName(), service.getApi());
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(@PathVariable String id, @RequestBody Service service) {
        try {
            return ResponseEntity.ok(serviceService.updateService(id, service));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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
}
