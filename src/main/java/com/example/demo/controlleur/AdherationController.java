package com.example.demo.controlleur;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Adheration;
import com.example.demo.entity.User;
import com.example.demo.servecies.AdherationService;

@RestController
@RequestMapping("/api/adheration")
public class AdherationController {

    @Autowired
    private AdherationService adherationService;

    /**
     * Create a new adheration request
     */
    @PostMapping("/request")
    public ResponseEntity<Adheration> createAdherationRequest(@RequestBody Adheration adheration) {
        Adheration createdRequest = adherationService.createAdherationRequest(adheration);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    /**
     * Get all adheration requests
     * Only accessible by Admin users
     */
    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Adheration>> getAllAdherationRequests() {
        List<Adheration> requests = adherationService.getAllAdherationRequests();
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    /**
     * Accept an adheration request
     * Only accessible by Admin users
     */
    @PostMapping("/accept/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> acceptAdherationRequest(
            @PathVariable String id, 
            @RequestBody(required = false) Map<String, String> payload) {
        
        String message = payload != null ? payload.get("message") : null;
        Map<String, Object> result = adherationService.acceptAdherationRequest(id, message);
        
        User createdUser = (User) result.get("user");
        String generatedPassword = (String) result.get("password");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Adheration request accepted successfully. Account credentials were sent to " + createdUser.getUsername());
        response.put("requestId", id);
        response.put("email", createdUser.getUsername());
        response.put("username", createdUser.getUsername());
        response.put("password", generatedPassword);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Refuse an adheration request
     * Only accessible by Admin users
     */
    @PostMapping("/refuse/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Adheration> refuseAdherationRequest(
            @PathVariable String id, 
            @RequestBody(required = false) Map<String, String> payload) {
        
        String message = payload != null ? payload.get("message") : null;
        Adheration updatedRequest = adherationService.refuseAdherationRequest(id, message);
        return new ResponseEntity<>(updatedRequest, HttpStatus.OK);
    }

    /**
     * Delete an adheration request
     * Only accessible by Admin users
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> deleteAdherationRequest(@PathVariable String id) {
        adherationService.deleteAdherationRequest(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
