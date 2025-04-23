package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AdherationRequest;

@Repository
public interface AdherationRequestRepository extends MongoRepository<AdherationRequest, String> {
    // Custom query methods can be added here if needed
}
