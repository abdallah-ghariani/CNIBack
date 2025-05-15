package com.example.demo.servecies;

// Imports
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ApiRequestDTO;
import com.example.demo.dto.UserCredentialsDto;
import com.example.demo.entity.Api;
import com.example.demo.entity.ApiRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ApiRepository;
import com.example.demo.repository.ApiRequestRepository;

@Service
public class ApiRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiRequestService.class);
    
    @Autowired
    private ApiRequestRepository apiRequestRepository;
    
    @Autowired
    private ApiRepository apiRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new API access request
     * This method now supports both authenticated and unauthenticated users
     */
    public ApiRequest createRequest(ApiRequestDTO dto) {
        // Verify the API exists
        Api api = apiRepository.findById(dto.getApiId())
            .orElseThrow(() -> new ResourceNotFoundException("API not found with ID: " + dto.getApiId()));
        
        // Create the request
        ApiRequest request = new ApiRequest();
        request.setApiId(dto.getApiId());
        
        // Try to get current user if authenticated
        String consumerId = null;
        try {
            User consumer = userService.getCurrentUser();
            consumerId = consumer.getId();
            logger.info("Creating new API access request for API ID: {} by authenticated consumer: {}", 
                      dto.getApiId(), consumer.getUsername());
        } catch (Exception e) {
            // User is not authenticated or there was an error getting the current user
            logger.info("Creating new API access request for API ID: {} by unauthenticated user with email: {}", 
                      dto.getApiId(), dto.getEmail());
        }
        
        // Set consumer ID if available
        request.setConsumerId(consumerId);
        
        // Set the provider ID from the API
        String providerId = api.getProviderId();
        if (providerId == null) {
            logger.warn("API with ID {} does not have a provider ID set", dto.getApiId());
        }
        request.setProviderId(providerId);
        
        // Set basic fields
        request.setName(dto.getName());
        request.setEmail(dto.getEmail());
        request.setSecteur(dto.getSecteur());
        request.setStructure(dto.getStructure());
        request.setMessage(dto.getMessage());
        
        // Set additional fields from updated DTO
        request.setService(dto.getService());       // Service filtering
        
        // Handle API name - if not provided in DTO, use API object name
        if (dto.getApiName() != null && !dto.getApiName().isEmpty()) {
            request.setApiName(dto.getApiName());
        } else {
            request.setApiName(api.getName());
        }
        
        // Set description if provided
        if (dto.getDescription() != null) {
            request.setDescription(dto.getDescription());
        }
        
        // Set metadata if provided
        if (dto.getMetadata() != null) {
            request.setMetadata(dto.getMetadata());
        }
        
        // Set request date (either from DTO or current date)
        if (dto.getRequestDate() != null) {
            request.setRequestDate(dto.getRequestDate());
        } else {
            request.setRequestDate(new Date());
        }
        
        // Set status (either from DTO or default to "pending")
        if (dto.getStatus() != null) {
            request.setStatus(dto.getStatus());
        } else {
            request.setStatus("pending");
        }
        
        return apiRequestRepository.save(request);
    }
    
    /**
     * Get all API access requests for APIs owned by the current provider
     * Returns all requests (pending, approved, rejected) for complete history
     */
    public List<ApiRequest> getRequestsForProvider() {
        // Get current user
        User provider = userService.getCurrentUser();
        logger.info("Getting all API access requests for provider: {}", provider.getUsername());
        
        // Method 1: Get requests by providerId directly (primary method)
        List<ApiRequest> requestsByProviderId = apiRequestRepository.findByProviderId(provider.getId());
        logger.info("Found {} API access requests by provider ID directly", requestsByProviderId.size());
        
        // Method 2: Get all APIs owned by this provider
        List<Api> providerApis = apiRepository.findByProviderId(provider.getId());
        
        if (!providerApis.isEmpty()) {
            // Get all API IDs
            List<String> apiIds = providerApis.stream()
                .map(Api::getId)
                .collect(Collectors.toList());
            
            // Find all requests for these APIs (regardless of status)
            List<ApiRequest> requestsByApiIds = apiRequestRepository.findByApiIdIn(apiIds);
            logger.info("Found {} API access requests by API IDs", requestsByApiIds.size());
            
            // Combine both result sets and remove duplicates
            if (!requestsByApiIds.isEmpty()) {
                requestsByProviderId.addAll(requestsByApiIds);
                // Remove duplicates by converting to a Set and back to List
                requestsByProviderId = requestsByProviderId.stream()
                    .distinct()
                    .collect(Collectors.toList());
            }
        } else {
            logger.info("No APIs found for provider: {}", provider.getUsername());
        }
        
        // For each request, ensure we have all the required details
        for (ApiRequest request : requestsByProviderId) {
            // Make sure we have the API name
            if (request.getApiName() == null || request.getApiName().isEmpty()) {
                try {
                    Api api = apiRepository.findById(request.getApiId())
                        .orElse(null);
                    if (api != null) {
                        request.setApiName(api.getName());
                    }
                } catch (Exception e) {
                    logger.warn("Could not fetch API name for request {}", request.getId());
                }
            }
        }
        
        logger.info("Found total of {} API access requests for provider: {}", requestsByProviderId.size(), provider.getUsername());
        return requestsByProviderId;
    }
    
    /**
     * Get API access requests for a specific API
     */
    public List<ApiRequest> getRequestsByApiId(String apiId) {
        logger.info("Getting all API access requests for API ID: {}", apiId);
        return apiRequestRepository.findByApiId(apiId);
    }
    
    /**
     * Get API access requests for a specific consumer
     */
    public List<ApiRequest> getRequestsByConsumerId(String consumerId) {
        logger.info("Getting all API access requests from consumer ID: {}", consumerId);
        return apiRequestRepository.findByConsumerId(consumerId);
    }
    
    /**
     * Get all API requests, regardless of status, consumer, or provider
     * This is useful for admin views and testing
     */
    public List<ApiRequest> getAllRequests() {
        logger.info("Getting all API requests");
        return apiRequestRepository.findAll();
    }
    
    /**
     * Get all API access requests made by the current authenticated consumer
     * Returns all requests regardless of status (pending, approved, rejected)
     */
    public List<ApiRequest> getRequestsForConsumer() {
        // Get current user
        User consumer = userService.getCurrentUser();
        logger.info("Getting all API access requests made by consumer: {}", consumer.getUsername());
        
        // Find all requests made by this consumer
        List<ApiRequest> requests = apiRequestRepository.findByConsumerId(consumer.getId());
        
        // For each request, ensure we have the API name
        for (ApiRequest request : requests) {
            if (request.getApiName() == null || request.getApiName().isEmpty()) {
                try {
                    Api api = apiRepository.findById(request.getApiId())
                        .orElse(null);
                    if (api != null) {
                        request.setApiName(api.getName());
                    }
                } catch (Exception e) {
                    logger.warn("Could not fetch API name for request {}", request.getId());
                }
            }
        }
        
        logger.info("Found {} API access requests for consumer: {}", requests.size(), consumer.getUsername());
        return requests;
    }
    
    /**
     * Get an API access request by ID
     */
    public ApiRequest getRequestById(String requestId) {
        logger.info("Getting API access request by ID: {}", requestId);
        return apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("API request not found with ID: " + requestId));
    }
    
    /**
     * Approve an API access request
     * Only the provider of the API can approve a request
     * When approved, a user account is created with a generated password
     * 
     * @return UserCredentialsDto containing the user's email, username, and password
     */
    public UserCredentialsDto approveRequest(String requestId) {
        User provider = userService.getCurrentUser();
        logger.info("Approving API access request ID: {} by provider: {}", 
                   requestId, provider.getUsername());
        
        // Find the request
        ApiRequest request = apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));
            
        // Verify the API belongs to this provider
        Api api = apiRepository.findById(request.getApiId())
            .orElseThrow(() -> new ResourceNotFoundException("API not found with ID: " + request.getApiId()));
        
        // Check if current user is an admin (admins can approve any request)
        boolean isAdmin = false;
        if (provider != null && provider.getAuthorities() != null) {
            isAdmin = provider.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().toLowerCase().contains("admin"));
            logger.info("User role check - Username: {}, IsAdmin: {}", provider.getUsername(), isAdmin);
        }
        
        // If not admin, check if providerId matches current user
        if (!isAdmin) {
            // Check if providerId is set and matches current user
            String providerId = api.getProviderId();
            if (providerId == null) {
                // If providerId is not set, log a warning and use the request's providerId if available
                logger.warn("API {} does not have a providerId set, using providerId from request", api.getId());
                providerId = request.getProviderId();
            }
            
            if (providerId == null || !providerId.equals(provider.getId())) {
                throw new UnauthorizedException("Not authorized to approve this request - must be API provider or admin");
            }
        } else {
            logger.info("Admin user {} is bypassing providerId check for approval", provider.getUsername());
        }
        
        // Check if request is already processed
        if (!request.getStatus().equals("pending")) {
            throw new BadRequestException("Request is already " + request.getStatus());
        }
        
        // Update request status
        request.setStatus("approved");
        apiRequestRepository.save(request);
        
        // Generate a random password for the new user
        String generatedPassword = generateSecurePassword();
        
        // Create a new user account using the request information
        String username = request.getEmail().split("@")[0]; // Use part of email as username
        userService.addUser(username, generatedPassword, "consumer");
        
        // Return the user credentials
        UserCredentialsDto credentials = new UserCredentialsDto(
            request.getEmail(),
            username,
            generatedPassword
        );
        
        logger.info("Created new user account for approved request. Username: {}", username);
        
        return credentials;
    }
    
    /**
     * Generates a secure random password
     */
    private String generateSecurePassword() {
        final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        final String NUMBER = "0123456789";
        final String SPECIAL = "!@#$%^&*()_-+=<>?";
        final String ALL_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12); // 12 characters long
        
        // Ensure we have at least one of each type
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        
        // Fill the rest with random characters
        for (int i = 4; i < 12; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Reject an API access request
     * Only the provider of the API can reject a request
     */
    public ApiRequest rejectRequest(String requestId) {
        User provider = userService.getCurrentUser();
        logger.info("Rejecting API access request ID: {} by provider: {}", 
                   requestId, provider.getUsername());
        
        // Find the request
        ApiRequest request = apiRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));
            
        // Verify the API belongs to this provider
        Api api = apiRepository.findById(request.getApiId())
            .orElseThrow(() -> new ResourceNotFoundException("API not found with ID: " + request.getApiId()));
        
        // Check if providerId is set and matches current user
        String providerId = api.getProviderId();
        if (providerId == null) {
            // If providerId is not set, log a warning and use the request's providerId if available
            logger.warn("API {} does not have a providerId set, using providerId from request", api.getId());
            providerId = request.getProviderId();
        }
        
        if (providerId == null || !providerId.equals(provider.getId())) {
            throw new UnauthorizedException("Not authorized to reject this request");
        }
        
        // Check if request is already processed
        if (!request.getStatus().equals("pending")) {
            throw new BadRequestException("Request is already " + request.getStatus());
        }
        
        // Update request status
        request.setStatus("rejected");
        
        return apiRequestRepository.save(request);
    }
}
