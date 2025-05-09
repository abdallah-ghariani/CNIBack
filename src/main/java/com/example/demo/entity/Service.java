package com.example.demo.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.querydsl.core.annotations.QueryEntity;

@Document(collection = "services")
@QueryEntity
public class Service {
	@Id
	private String id;
	private String name;
	private String description;
	
	// Relationship with APIs (one service can have many APIs)
	@DocumentReference(lazy = true)
	private List<Api> apis;
	
	private String createdBy; // ID of the admin who created this service
	private boolean valid = true;

	public Service() {
	}
	
	public Service(String name, String description) {
		this.name = name;
		this.description = description;
	}

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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<Api> getApis() {
		return apis;
	}
	
	public void setApis(List<Api> apis) {
		this.apis = apis;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
