package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ApiRequest;

@Repository
public interface ApiRequestRepository extends MongoRepository<ApiRequest, String> {
    
    // Find all requests for APIs owned by a specific provider where API ID is in the given list
    List<ApiRequest> findByApiIdInAndStatus(List<String> apiIds, String status);
    
    // Find all requests by consumer ID
    List<ApiRequest> findByConsumerId(String consumerId);
    
    // Find all requests by API ID
    List<ApiRequest> findByApiId(String apiId);
    
    // Find all requests by API ID and status
    List<ApiRequest> findByApiIdAndStatus(String apiId, String status);
    
    // Find all requests by provider ID
    List<ApiRequest> findByProviderId(String providerId);
    
    // Find all requests by provider ID and status
    List<ApiRequest> findByProviderIdAndStatus(String providerId, String status);
    
    // Find all requests where the API ID is in the given list
    List<ApiRequest> findByApiIdIn(List<String> apiIds);
    
    // Find all requests by sector and status with pagination
    Page<ApiRequest> findBySecteurAndStatus(String secteur, String status, Pageable pageable);
}
