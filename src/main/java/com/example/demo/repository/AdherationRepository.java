package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Adheration;

@Repository
public interface AdherationRepository extends MongoRepository<Adheration, String> {
    // Additional query methods can be added here if needed
}
