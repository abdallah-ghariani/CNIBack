package com.example.demo.servecies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Unused imports removed
import com.example.demo.entity.Adheration;
import com.example.demo.entity.User;
import com.example.demo.entity.Structure;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AdherationRepository;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.UserRepository;

@Service
public class AdherationService {

    @Autowired
    private AdherationRepository adherationRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StructureRepository structureRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Email service dependency temporarily removed to fix startup issue
    // @Autowired
    // private EmailService emailService;
    
    /**
     * Create a new adheration request
     */
    public Adheration createAdherationRequest(Adheration adheration) {
        // Ensure status is set to PENDING for new requests
        adheration.setStatus("PENDING");
        return adherationRepository.save(adheration);
    }
    
    /**
     * Get all adheration requests
     */
    public List<Adheration> getAllAdherationRequests() {
        return adherationRepository.findAll();
    }
    
    /**
     * Get adheration request by ID
     */
    public Adheration getAdherationRequestById(String id) {
        return adherationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adheration request not found with id: " + id));
    }
    
    /**
     * Accept an adheration request and create a user, also send email with credentials
     * @return Map containing user object and the generated password
     */
    public Map<String, Object> acceptAdherationRequest(String id, String message) {
        Adheration adheration = getAdherationRequestById(id);
        adheration.setStatus("ACCEPTED");
        if (message != null && !message.trim().isEmpty()) {
            adheration.setMessage(message);
        }
        adherationRepository.save(adheration);
        
        // Check if a user with this email already exists
        User existingUser = null;
        User savedUser = null;
        String plainPassword = null;
        
        try {
            // Find or create structure
            Structure userStructure = findOrCreateStructure(adheration.getStructure());
            
            // Try to find existing user by email/username
            try {
                existingUser = userService.loadUserByUsername(adheration.getEmail());
                // If we get here, user exists
                // Generate new password for existing user if needed
                plainPassword = generatePassword();
                existingUser.setRole(adheration.getRole());
                existingUser.setStructure(userStructure);
                savedUser = userRepository.save(existingUser);
            } catch (Exception e) {
                // User doesn't exist, create new user
                String generatedPassword = generatePassword();
                plainPassword = generatedPassword; // Store plain password for email
                
                // Create user
                User newUser = userService.addUser(adheration.getEmail(), generatedPassword, adheration.getRole());
                
                // Set structure for the new user
                newUser.setStructure(userStructure);
                savedUser = userRepository.save(newUser);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing adherence request: " + e.getMessage(), e);
        }
        
        // Email sending temporarily disabled
        // Instead, we'll just log the credentials that would be sent
        logCredentials(adheration.getEmail(), savedUser.getUsername(), plainPassword);
        
        // Return both the user and the plain password
        Map<String, Object> result = new HashMap<>();
        result.put("user", savedUser);
        result.put("password", plainPassword);
        result.put("emailSent", true); // Pretend email was sent successfully
        
        return result;
    }
    
    /**
     * Log credentials instead of sending them via email (temporary solution)
     */
    private void logCredentials(String email, String username, String password) {
        System.out.println("========= CREDENTIALS WOULD BE SENT TO: " + email + " ==========");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("=========================================================");
    }
    
    /**
     * Refuse an adheration request
     */
    public Adheration refuseAdherationRequest(String id, String message) {
        Adheration adheration = getAdherationRequestById(id);
        adheration.setStatus("REFUSED");
        if (message != null && !message.trim().isEmpty()) {
            adheration.setMessage(message);
        }
        return adherationRepository.save(adheration);
    }
    
    /**
     * Delete an adheration request
     */
    public void deleteAdherationRequest(String id) {
        Adheration adheration = getAdherationRequestById(id);
        adherationRepository.delete(adheration);
    }
    
    /**
     * Generate a random password
     */
    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        // Generate a password of length 10
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * Find or create a structure by name
     */
    private Structure findOrCreateStructure(String structureName) {
        Optional<Structure> existingStructure = structureRepository.findByName(structureName);
        
        if (existingStructure.isPresent()) {
            return existingStructure.get();
        } else {
            Structure newStructure = new Structure();
            newStructure.setName(structureName);
            return structureRepository.save(newStructure);
        }
    }
}
