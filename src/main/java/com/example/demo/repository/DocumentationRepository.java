package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.Documentation;

public interface DocumentationRepository extends MongoRepository<Documentation, String>  {
	

}
