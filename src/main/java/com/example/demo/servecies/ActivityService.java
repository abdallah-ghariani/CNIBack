package com.example.demo.servecies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Activity;
import com.example.demo.entity.ActivityType;
import com.example.demo.entity.Api;
import com.example.demo.entity.Secteur;
import com.example.demo.entity.User;
import com.example.demo.repository.ActivityRepository;
import com.example.demo.repository.SecteurRepository;
import com.example.demo.repository.UserRepository;

@Service
public class ActivityService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private SecteurRepository secteurRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Create and save a new activity
     * 
     * @param type The type of activity
     * @param description A human-readable description of the activity
     * @return The saved activity
     */
    public Activity createActivity(ActivityType type, String description) {
        Activity activity = new Activity(type, description);
        return activityRepository.save(activity);
    }
    
    /**
     * Create a detailed activity with entity information
     * 
     * @param type The type of activity
     * @param description A human-readable description of the activity
     * @param entityId ID of the related entity (API, User, etc.)
     * @param entityName Name of the related entity
     * @return The saved activity
     */
    public Activity createDetailedActivity(ActivityType type, String description, 
            String entityId, String entityName) {
        
        Activity activity = new Activity(type, description);
        activity.setEntityId(entityId);
        activity.setEntityName(entityName);
        
        return activityRepository.save(activity);
    }
    
    /**
     * Create an API-related activity with API and user information
     * 
     * @param type The type of activity
     * @param description A human-readable description
     * @param api The related API entity
     * @param user The user who performed the action (can be null)
     * @return The saved activity
     */
    public Activity createApiActivity(ActivityType type, String description, Api api, User user) {
        Activity activity = new Activity(type, description);
        
        // Set API information
        if (api != null) {
            activity.setEntityId(api.getId());
            activity.setEntityName(api.getName());
            
            // Set sector information if available
            if (api.getSecteur() != null && !api.getSecteur().isEmpty()) {
                activity.setSectorId(api.getSecteur());
                
                // Try to get sector name from repository
                secteurRepository.findById(api.getSecteur())
                    .ifPresent(sector -> activity.setSectorName(sector.getName()));
            }
            
            // Set service information if available
            if (api.getService() != null && !api.getService().isEmpty()) {
                activity.setServiceId(api.getService());
                activity.setServiceName(api.getService()); // Simplified, ideally would get the name from a service repository
            }
        }
        
        // Set user information if available
        if (user != null) {
            activity.setUserId(user.getId());
            activity.setUsername(user.getUsername());
        }
        
        return activityRepository.save(activity);
    }
    
    /**
     * Create a user-related activity
     * 
     * @param type The type of activity
     * @param description A human-readable description
     * @param user The related user entity
     * @return The saved activity
     */
    public Activity createUserActivity(ActivityType type, String description, User user) {
        Activity activity = new Activity(type, description);
        
        if (user != null) {
            activity.setEntityId(user.getId());
            activity.setEntityName(user.getUsername());
            activity.setUserId(user.getId());
            activity.setUsername(user.getUsername());
            
            // Set sector information if available
            if (user.getSecteur() != null) {
                Secteur sector = user.getSecteur();
                activity.setSectorId(sector.getId());
                activity.setSectorName(sector.getName());
            }
        }
        
        return activityRepository.save(activity);
    }
    
    /**
     * Create a sector-related activity
     * 
     * @param type The type of activity
     * @param description A human-readable description
     * @param sector The related sector entity
     * @param user The user who performed the action (can be null)
     * @return The saved activity
     */
    public Activity createSectorActivity(ActivityType type, String description, Secteur sector, User user) {
        Activity activity = new Activity(type, description);
        
        if (sector != null) {
            activity.setEntityId(sector.getId());
            activity.setEntityName(sector.getName());
            activity.setSectorId(sector.getId());
            activity.setSectorName(sector.getName());
        }
        
        if (user != null) {
            activity.setUserId(user.getId());
            activity.setUsername(user.getUsername());
        }
        
        return activityRepository.save(activity);
    }
    
    /**
     * Get recent activities with optional filtering by type and date
     * 
     * @param limit Maximum number of activities to return
     * @param activityTypes Optional list of activity types to filter by
     * @param since Optional date to filter activities after
     * @return List of activities matching the criteria
     */
    public List<Activity> getRecentActivities(int limit, List<ActivityType> activityTypes, Date since) {
        // Create a pageable object for sorting and limiting results
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        // Apply filters based on provided parameters
        if (activityTypes != null && !activityTypes.isEmpty() && since != null) {
            // Filter by both type and date
            return activityRepository.findByTypeInAndTimestampAfter(activityTypes, since, pageable);
        } else if (activityTypes != null && !activityTypes.isEmpty()) {
            // Filter by type only
            return activityRepository.findByTypeIn(activityTypes, pageable);
        } else if (since != null) {
            // Filter by date only
            return activityRepository.findByTimestampAfter(since, pageable);
        } else {
            // No filters, just return recent activities
            return activityRepository.findAll(pageable).getContent();
        }
    }
    
    /**
     * Get activities with comprehensive filtering options
     * 
     * @param limit Maximum number of activities to return
     * @param activityTypes Optional list of activity types to filter by
     * @param since Optional date to filter activities after
     * @param entityId Optional entity ID to filter by
     * @param userId Optional user ID to filter by
     * @return List of activities matching all provided criteria
     */
    public List<Activity> getFilteredActivities(int limit, List<ActivityType> activityTypes, 
            Date since, String entityId, String userId) {
        
        // Create a pageable object for sorting and limiting results
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        // Build criteria for MongoDB query
        List<Criteria> criteriaList = new ArrayList<>();
        
        if (activityTypes != null && !activityTypes.isEmpty()) {
            criteriaList.add(Criteria.where("type").in(activityTypes));
        }
        
        if (since != null) {
            criteriaList.add(Criteria.where("timestamp").gt(since));
        }
        
        if (entityId != null && !entityId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("entityId").is(entityId));
        }
        
        if (userId != null && !userId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }
        
        // If no filters provided, just return recent activities
        if (criteriaList.isEmpty()) {
            return activityRepository.findAll(pageable).getContent();
        }
        
        // Combine all criteria with AND operator
        Criteria combinedCriteria = new Criteria().andOperator(
                criteriaList.toArray(new Criteria[criteriaList.size()]));
        
        // Create match operation with the combined criteria
        MatchOperation matchOperation = Aggregation.match(combinedCriteria);
        
        // Create sort and limit operations
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.sort(Sort.Direction.DESC, "timestamp"),
                Aggregation.limit(limit)
        );
        
        // Execute the aggregation query
        AggregationResults<Activity> results = mongoTemplate.aggregate(
                aggregation, "activities", Activity.class);
        
        return results.getMappedResults();
    }
    
    /**
     * Get activities related to a specific entity
     * 
     * @param entityId ID of the entity to get activities for
     * @param limit Maximum number of activities to return
     * @return List of activities for the entity
     */
    public List<Activity> getActivitiesByEntityId(String entityId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return activityRepository.findByEntityId(entityId, pageable);
    }
    
    /**
     * Get activities performed by a specific user
     * 
     * @param userId ID of the user to get activities for
     * @param limit Maximum number of activities to return
     * @return List of activities for the user
     */
    public List<Activity> getActivitiesByUserId(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return activityRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Get activities related to the current authenticated user
     * 
     * @param limit Maximum number of activities to return
     * @return List of activities for the current user
     */
    public List<Activity> getCurrentUserActivities(int limit) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the user by username
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            logger.warn("Could not find user record for authenticated user: {}", username);
            return new ArrayList<>();
        }
        
        // Get user's ID
        String userId = currentUser.getId();
        
        // Use the repository method to get activities for this user
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return activityRepository.findByUserIdOrEntityId(userId, pageable);
    }
    
    /**
     * Get dashboard data including activity counts by type and time period
     * 
     * @param days Number of days to look back for the dashboard data
     * @return Map of dashboard data metrics
     */
    public Map<String, Object> getDashboardData(int days) {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Calculate the date for 'days' ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        Date sinceDate = calendar.getTime();
        
        // Match activities after the since date
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("timestamp").gt(sinceDate));
        
        // Group by activity type and count
        GroupOperation groupByType = Aggregation.group("type")
                .count().as("count");
        
        // Execute aggregation for activity type counts
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupByType
        );
        
        // We can't directly parameterize with Map<String, Object> due to type erasure
        // Using raw type and then working with it safely
        @SuppressWarnings("rawtypes")
        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, "activities", Map.class);
        
        // Transform the results into a more user-friendly format
        Map<String, Integer> activityTypeCounts = new HashMap<>();
        
        // Process each result, treating it as a Map with String keys
        for (Object resultObj : results.getMappedResults()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) resultObj;
            activityTypeCounts.put(result.get("_id").toString(), 
                    Integer.parseInt(result.get("count").toString()));
        }
        
        // Add activity type counts to the dashboard data
        dashboardData.put("activityTypeCounts", activityTypeCounts);
        
        // Add total count of activities in the time period
        long totalActivities = activityRepository.countByTimestampAfter(sinceDate);
        dashboardData.put("totalActivities", totalActivities);
        
        // Get the most recent activities
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<Activity> recentActivities = activityRepository.findByTimestampAfter(sinceDate, pageable);
        dashboardData.put("recentActivities", recentActivities);
        
        return dashboardData;
    }
    
    /**
     * Parse a comma-separated string of activity types into a list of ActivityType enums
     * 
     * @param typesString Comma-separated string of activity types
     * @return List of ActivityType enums
     */
    public List<ActivityType> parseActivityTypes(String typesString) {
        if (typesString == null || typesString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Arrays.stream(typesString.split(","))
                    .map(String::trim)
                    .map(ActivityType::valueOf)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid activity type in: {}", typesString);
            return null;
        }
    }
    
    /**
     * Log an API request activity
     * 
     * @param requestId ID of the API request
     * @param apiId ID of the requested API
     * @param apiName Name of the requested API
     * @param userId ID of the requesting user
     * @param username Username of the requesting user
     * @param type Type of activity (e.g., API_REQUEST_SUBMITTED, API_REQUEST_APPROVED)
     * @param description Human-readable description of the activity
     * @return The saved activity
     */
    public Activity logApiRequestActivity(String requestId, String apiId, String apiName, 
            String userId, String username, ActivityType type, String description) {
        
        Activity activity = new Activity(type, description);
        
        // Set request as the entity
        activity.setEntityId(requestId);
        activity.setEntityName("API Request: " + apiName);
        
        // Set API information
        if (apiId != null && !apiId.isEmpty()) {
            activity.setEntityId(apiId);
            activity.setEntityName(apiName);
        }
        
        // Set user information
        if (userId != null && !userId.isEmpty()) {
            activity.setUserId(userId);
            activity.setUsername(username);
        }
        
        return activityRepository.save(activity);
    }
}
