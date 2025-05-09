package com.example.demo.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.querydsl.core.annotations.QueryEntity;
@Document(collection = "secteurs")
@QueryEntity

public class Secteur {
	@Id
	String id;
	String name;
	
	// Relationship with Structures (one secteur has many structures)
	@DocumentReference(lazy = true)
	private List<Structure> structures;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Structure> getStructures() {
		return structures;
	}
	public void setStructures(List<Structure> structures) {
		this.structures = structures;
	}
}
