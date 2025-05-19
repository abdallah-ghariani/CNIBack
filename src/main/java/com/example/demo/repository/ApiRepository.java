package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Api;

@Repository
public interface ApiRepository extends MongoRepository<Api, String> {
    // Find all APIs owned by a specific provider
    List<Api> findByProviderId(String providerId);
    
    // Find all APIs for a specific structure
    List<Api> findByStructure(String structure);
    
    // Find all APIs for a specific service
    List<Api> findByService(String service);
}
