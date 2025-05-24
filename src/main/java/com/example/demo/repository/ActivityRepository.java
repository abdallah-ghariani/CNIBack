package com.example.demo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Activity;
import com.example.demo.entity.ActivityType;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {
    
    // Find activities by type
    List<Activity> findByType(ActivityType type, Pageable pageable);
    
    // Find activities by multiple types
    List<Activity> findByTypeIn(List<ActivityType> types, Pageable pageable);
    
    // Find activities after a certain date
    List<Activity> findByTimestampAfter(Date timestamp, Pageable pageable);
    
    // Count activities after a certain date
    long countByTimestampAfter(Date timestamp);
    
    // Find activities by types and after a certain date
    List<Activity> findByTypeInAndTimestampAfter(List<ActivityType> types, Date timestamp, Pageable pageable);
    
    // Find activities by entity ID
    List<Activity> findByEntityId(String entityId, Pageable pageable);
    
    // Find activities by sector ID
    List<Activity> findBySectorId(String sectorId, Pageable pageable);
    
    // Find activities by user ID
    List<Activity> findByUserId(String userId, Pageable pageable);
    
    // Find activities by user ID or entity ID (activities related to a user)
    @Query("{$or: [{userId: ?0}, {entityId: ?0}]}")
    List<Activity> findByUserIdOrEntityId(String userId, Pageable pageable);
    
    // Find activities by type and entity ID
    List<Activity> findByTypeAndEntityId(ActivityType type, String entityId, Pageable pageable);
}
