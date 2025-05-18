package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ApiAccessRequest;

@Repository
public interface ApiAccessRequestRepository extends MongoRepository<ApiAccessRequest, String> {
    // Basic CRUD operations
    List<ApiAccessRequest> findByProviderId(String providerId);
    List<ApiAccessRequest> findByConsumerId(String consumerId);
    List<ApiAccessRequest> findByApiId(String apiId);
    List<ApiAccessRequest> findByStatus(String status);
    
    // Paginated queries
    Page<ApiAccessRequest> findByConsumerId(String consumerId, Pageable pageable);
    Page<ApiAccessRequest> findByApiSector(String apiSector, Pageable pageable);
    Page<ApiAccessRequest> findByConsumerIdAndStatus(String consumerId, String status, Pageable pageable);
    Page<ApiAccessRequest> findByApiSectorAndStatus(String apiSector, String status, Pageable pageable);
    
    // Combined queries
    List<ApiAccessRequest> findByConsumerIdAndApiSector(String consumerId, String apiSector);
    List<ApiAccessRequest> findByApiSectorAndStatus(String apiSector, String status);
}
