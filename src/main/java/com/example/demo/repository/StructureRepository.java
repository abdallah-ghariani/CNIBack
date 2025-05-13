package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.ListQuerydslPredicateExecutor;

import com.example.demo.entity.Structure;

public interface StructureRepository extends MongoRepository<Structure, String>, ListQuerydslPredicateExecutor<Structure>{
    Optional<Structure> findByName(String name);
}
