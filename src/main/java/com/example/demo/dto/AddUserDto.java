package com.example.demo.dto;

public class AddUserDto {
	String username;
	String password;
	String role;
	String secteurId;
	String structureId;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getSecteurId() {
		return secteurId;
	}
	
	public void setSecteurId(String secteurId) {
		this.secteurId = secteurId;
	}
	
	public String getStructureId() {
		return structureId;
	}
	
	public void setStructureId(String structureId) {
		this.structureId = structureId;
	}
}
