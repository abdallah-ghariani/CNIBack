package com.example.demo.controlleur;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Activity;
import com.example.demo.entity.ActivityType;
import com.example.demo.servecies.ActivityService;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
    
    @Autowired
    private ActivityService activityService;
    
    /**
     * Get recent activities with optional filtering
     * 
     * @param limit Maximum number of activities to return (default: 10)
     * @param types Optional comma-separated list of activity types to filter by
     * @param since Optional ISO timestamp to filter activities after
     * @return List of activities matching the criteria
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Activity>> getRecentActivities(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String types,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String userId) {
        
        logger.info("Getting recent activities - limit: {}, types: {}, since: {}, entityId: {}, userId: {}", 
                limit, types, since, entityId, userId);
        
        // Parse activity types if provided
        List<ActivityType> activityTypes = null;
        if (types != null && !types.trim().isEmpty()) {
            activityTypes = activityService.parseActivityTypes(types);
        }
        
        // Parse the since date if provided
        Date sinceDate = null;
        if (since != null && !since.trim().isEmpty()) {
            try {
                // Try to parse as ISO-8601 timestamp
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                sinceDate = isoFormat.parse(since);
                logger.info("Parsed since date: {}", sinceDate);
            } catch (ParseException e) {
                logger.warn("Failed to parse since date '{}', ignoring this filter", since);
            }
        }
        
        // Get activities using the service with enhanced filtering
        List<Activity> activities = activityService.getFilteredActivities(limit, activityTypes, sinceDate, entityId, userId);
        logger.info("Found {} recent activities", activities.size());
        
        return ResponseEntity.ok(activities);
    }
    
    /**
     * Get a dashboard view of recent activities grouped by type
     * 
     * @param days Number of days to look back (default: 30)
     * @return Map of activity types to counts
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getActivityDashboard(
            @RequestParam(defaultValue = "30") int days) {
        
        logger.info("Getting activity dashboard for the last {} days", days);
        
        Map<String, Object> dashboardData = activityService.getDashboardData(days);
        
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get activities related to a specific entity (API, User, etc.)
     * 
     * @param entityId ID of the entity to get activities for
     * @param limit Maximum number of activities to return (default: 20)
     * @return List of activities for the entity
     */
    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<Activity>> getEntityActivities(
            @PathVariable String entityId,
            @RequestParam(defaultValue = "20") int limit) {
        
        logger.info("Getting activities for entity: {}", entityId);
        
        List<Activity> activities = activityService.getActivitiesByEntityId(entityId, limit);
        
        return ResponseEntity.ok(activities);
    }
    
    /**
     * Get activities performed by a specific user
     * 
     * @param userId ID of the user to get activities for
     * @param limit Maximum number of activities to return (default: 20)
     * @return List of activities for the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Activity>> getUserActivities(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {
        
        logger.info("Getting activities for user: {}", userId);
        
        List<Activity> activities = activityService.getActivitiesByUserId(userId, limit);
        
        return ResponseEntity.ok(activities);
    }
    
    /**
     * Get activities related to the current user (both created by them and affecting their entities)
     * Requires authentication.
     * 
     * @param limit Maximum number of activities to return (default: 20)
     * @return List of activities for the current user
     */
    @GetMapping("/my-activities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Activity>> getCurrentUserActivities(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<Activity> activities = activityService.getCurrentUserActivities(limit);
        
        return ResponseEntity.ok(activities);
    }
}
