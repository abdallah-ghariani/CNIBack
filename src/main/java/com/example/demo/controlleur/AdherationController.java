package com.example.demo.controlleur;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AdherationRequestDto;
import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.AdherationRequest;
import com.example.demo.servecies.AdherationService;

import java.util.List;

@RestController
@RequestMapping("/api/adheration")
public class AdherationController {

    private final AdherationService adherationService;
    
    public AdherationController(AdherationService adherationService) {
        this.adherationService = adherationService;
    }
    
    /**
     * Submit a new adheration request
     * This endpoint aligns with the requested API specification
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse> submitRequest(@RequestBody AdherationRequestDto requestDto) {
        try {
            // Validate required fields
            if (requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name is required"));
            }
            
            if (requestDto.getStructure() == null || requestDto.getStructure().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Structure is required"));
            }
            
            if (requestDto.getSecteur() == null || requestDto.getSecteur().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Secteur is required"));
            }
            
            // Validate role field
            if (requestDto.getRole() == null || requestDto.getRole().trim().isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Role is required"));
            }
            
            // Validate role values
            String role = requestDto.getRole().trim().toLowerCase();
            if (!("provider".equals(role) || "consumer".equals(role))) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Role must be either 'provider' or 'consumer'"));
            }
            
            AdherationRequest savedRequest = adherationService.createAdherationRequest(requestDto);
            return ResponseEntity.ok(new ApiResponse(
                true, 
                "Adheration request submitted successfully", 
                savedRequest.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error submitting adheration request: " + e.getMessage()));
        }
    }
    
    /**
     * Get all adheration requests
     */
    @GetMapping("/requests")
    public ResponseEntity<List<AdherationRequest>> getAllRequests() {
        List<AdherationRequest> requests = adherationService.getAllRequests();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Accept an adheration request
     */
    @PostMapping("/accept/{id}")
    public ResponseEntity<ApiResponse> acceptRequest(@PathVariable String id) {
        AdherationRequest updatedRequest = adherationService.acceptRequest(id);
        if (updatedRequest != null) {
            return ResponseEntity.ok(new ApiResponse(
                true, 
                "Adheration request accepted successfully", 
                updatedRequest.getId()
            ));
        } else {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Adheration request not found"));
        }
    }
    
    /**
     * Refuse an adheration request
     */
    @PostMapping("/refuse/{id}")
    public ResponseEntity<ApiResponse> refuseRequest(@PathVariable String id) {
        AdherationRequest updatedRequest = adherationService.refuseRequest(id);
        if (updatedRequest != null) {
            return ResponseEntity.ok(new ApiResponse(
                true, 
                "Adheration request refused successfully", 
                updatedRequest.getId()
            ));
        } else {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Adheration request not found"));
        }
    }
    
    /**
     * Delete an adheration request
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteRequest(@PathVariable String id) {
        boolean deleted = adherationService.deleteRequest(id);
        if (deleted) {
            return ResponseEntity.ok(new ApiResponse(
                true, 
                "Adheration request deleted successfully", 
                id
            ));
        } else {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Adheration request not found"));
        }
    }
    
    /**
     * Alternative endpoint for submitting requests (for backward compatibility)
     * Based on previous implementation mentioned in project history
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse> submitRequestAlternative(@RequestBody AdherationRequestDto requestDto) {
        return submitRequest(requestDto);
    }
}
