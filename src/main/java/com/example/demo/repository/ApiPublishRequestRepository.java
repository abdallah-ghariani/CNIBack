package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ApiPublishRequest;

@Repository
public interface ApiPublishRequestRepository extends MongoRepository<ApiPublishRequest, String> {
    List<ApiPublishRequest> findByProviderId(String providerId);
    List<ApiPublishRequest> findByApiId(String apiId);
    List<ApiPublishRequest> findByStatus(String status);
}
