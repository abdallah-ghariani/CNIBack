package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ApiAccessRequest;

@Repository
public interface ApiAccessRequestRepository extends MongoRepository<ApiAccessRequest, String> {
    List<ApiAccessRequest> findByProviderId(String providerId);
    List<ApiAccessRequest> findByConsumerId(String consumerId);
    List<ApiAccessRequest> findByApiId(String apiId);
    List<ApiAccessRequest> findByStatus(String status);
}
