package com.example.demo.entity;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.querydsl.core.annotations.QueryEntity;

@Document(collection = "structures")
@QueryEntity
public class Structure {
	@Id
	String id;
	String name;
	
	// Relationship with Secteur
	@DocumentReference
	private Secteur secteur;
	
	// Relationship with Users (one structure has many users)
	@DocumentReference(lazy = true)
	private List<User> users;
	
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
	
	public Secteur getSecteur() {
		return secteur;
	}
	public void setSecteur(Secteur secteur) {
		this.secteur = secteur;
	}
	
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
}
