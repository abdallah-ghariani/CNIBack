package com.example.demo.servecies;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import com.example.demo.dto.AdherationRequestDto;
import com.example.demo.entity.AdherationRequest;
import com.example.demo.repository.AdherationRequestRepository;

@Service
public class AdherationService {

    private final AdherationRequestRepository adherationRequestRepository;
    private final UserService userService;
    private final StructureService structureService;
    private final SecteurService secteurService;

    @Autowired
    public AdherationService(
            AdherationRequestRepository adherationRequestRepository,
            UserService userService,
            StructureService structureService,
            SecteurService secteurService) {
        this.adherationRequestRepository = adherationRequestRepository;
        this.userService = userService;
        this.structureService = structureService;
        this.secteurService = secteurService;
    }

    /**
     * Create a new adheration request
     * 
     * @param requestDto The DTO containing request data
     * @return The created AdherationRequest entity
     */
    public AdherationRequest createAdherationRequest(AdherationRequestDto requestDto) {
        AdherationRequest adherationRequest = new AdherationRequest(
            requestDto.getName(),
            requestDto.getStructure(),
            requestDto.getSecteur(),
            requestDto.getRole(),
            requestDto.getMessage()
        );
        
        // Apply different processing based on role
        if ("provider".equals(requestDto.getRole())) {
            // Providers might need additional verification or different workflow
            // For now, just keeping as PENDING
        } else if ("consumer".equals(requestDto.getRole())) {
            // Consumers might have a different workflow
            // For now, just keeping as PENDING
        }
        
        return adherationRequestRepository.save(adherationRequest);
    }
    
    /**
     * Get all adheration requests
     * 
     * @return List of all adheration requests
     */
    public java.util.List<AdherationRequest> getAllRequests() {
        return adherationRequestRepository.findAll();
    }
    
    /**
     * Accept an adheration request
     * When a request is accepted, create the corresponding user, structure, and secteur
     * 
     * @param id The request ID
     * @return The updated request or null if not found
     */
    public AdherationRequest acceptRequest(String id) {
        AdherationRequest request = adherationRequestRepository.findById(id).orElse(null);
        
        if (request == null) {
            return null;
        }
        
        try {
            // Create or verify the secteur exists
            String secteurName = request.getSecteur();
            ensureSecteurExists(secteurName);
            
            // Create or verify the structure exists
            String structureName = request.getStructure();
            ensureStructureExists(structureName);
            
            // Generate a unique username based on the name
            String username = generateUsername(request.getName());
            
            // Generate a random password - in a real system, this would be emailed to the user
            String password = UUID.randomUUID().toString().substring(0, 8);
            
            // Create the user with the appropriate role, structure, and secteur
            userService.addUser(username, password, request.getRole(), structureName, secteurName);
            
            // Update the request status
            request.setStatus("ACCEPTED");
            return adherationRequestRepository.save(request);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to process acceptance: " + e.getMessage());
        }
    }
    
    /**
     * Ensure that a secteur with the given name exists
     * If it doesn't exist, create it
     * 
     * @param secteurName The name of the secteur
     * @return The ID of the existing or newly created secteur
     */
    private String ensureSecteurExists(String secteurName) {
        try {
            // Try to create the secteur
            return secteurService.addSecteur(secteurName).getId();
        } catch (Exception e) {
            // If creation fails (likely because it already exists), try to find it
            return secteurService.getAll(org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent()
                .stream()
                .filter(s -> s.getName().equalsIgnoreCase(secteurName))
                .findFirst()
                .map(s -> s.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Could not create or find secteur: " + secteurName));
        }
    }
    
    /**
     * Ensure that a structure with the given name exists
     * If it doesn't exist, create it
     * 
     * @param structureName The name of the structure
     * @return The ID of the existing or newly created structure
     */
    private String ensureStructureExists(String structureName) {
        try {
            // Try to create the structure
            return structureService.addStructure(structureName).getId();
        } catch (Exception e) {
            // If creation fails (likely because it already exists), try to find it
            return structureService.getAll(org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent()
                .stream()
                .filter(s -> s.getName().equalsIgnoreCase(structureName))
                .findFirst()
                .map(s -> s.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Could not create or find structure: " + structureName));
        }
    }
    
    /**
     * Generate a unique username based on a person's name
     * 
     * @param fullName The full name to generate a username from
     * @return A unique username
     */
    private String generateUsername(String fullName) {
        // Remove spaces and special characters, convert to lowercase
        String baseName = fullName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        
        // Ensure the username is at least 5 characters
        if (baseName.length() < 5) {
            baseName = baseName + "user";
        }
        
        // Try to find a unique username by appending a number if necessary
        String username = baseName;
        int counter = 1;
        
        while (true) {
            try {
                // Check if the username exists
                userService.loadUserByUsername(username);
                // If we get here, the username exists, try another one
                username = baseName + counter;
                counter++;
            } catch (Exception e) {
                // Username doesn't exist, we can use it
                return username;
            }
        }
    }
    
    /**
     * Refuse an adheration request
     * 
     * @param id The request ID
     * @return The updated request or null if not found
     */
    public AdherationRequest refuseRequest(String id) {
        return updateRequestStatus(id, "REFUSED");
    }
    
    /**
     * Delete an adheration request
     * 
     * @param id The request ID
     * @return true if deleted, false if not found
     */
    public boolean deleteRequest(String id) {
        if (adherationRequestRepository.existsById(id)) {
            adherationRequestRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private AdherationRequest updateRequestStatus(String id, String status) {
        return adherationRequestRepository.findById(id)
            .map(request -> {
                request.setStatus(status);
                return adherationRequestRepository.save(request);
            })
            .orElse(null);
    }
}
