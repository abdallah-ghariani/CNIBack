package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.ListQuerydslPredicateExecutor;

import com.example.demo.entity.Secteur;


public interface SecteurRepository  extends MongoRepository<Secteur, String>, ListQuerydslPredicateExecutor<Secteur> {

}
