package com.example.demo.controlleur;

import com.example.demo.dto.AddUserDto;
import com.example.demo.entity.Secteur;
import com.example.demo.entity.Structure;
import com.example.demo.entity.User;
import com.example.demo.repository.SecteurRepository;
import com.example.demo.repository.StructureRepository;
import com.example.demo.servecies.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private SecteurRepository secteurRepository;
	
	@Autowired
	private StructureRepository structureRepository;
	@PreAuthorize("hasAuthority('admin')")
	@PostMapping()
	public User addUser(@RequestBody AddUserDto user) {
		// Basic validation
		if (user.getUsername() == null || user.getPassword() == null || user.getRole() == null) {
			throw new IllegalArgumentException("Username, password, and role are required");
		}
		
		// Get secteur and structure if IDs are provided
		Secteur secteur = null;
		Structure structure = null;
		
		if (user.getSecteurId() != null && !user.getSecteurId().isEmpty()) {
			secteur = secteurRepository.findById(user.getSecteurId())
				.orElse(null);
		}
		
		if (user.getStructureId() != null && !user.getStructureId().isEmpty()) {
			structure = structureRepository.findById(user.getStructureId())
				.orElse(null);
		}
		
		// Create user with all available information
		return userService.addUser(user.getUsername(), user.getPassword(), user.getRole(), secteur, structure);
	}
	@PreAuthorize("hasAuthority('admin')")
	@GetMapping("/{id}")
	public User getUserById(@PathVariable String id) {
		return userService.getUserById(id);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@GetMapping()
	public Page<User> getallusers(Pageable page, @RequestParam(required = false) String username,  @RequestParam(required = false) String role) {
		return userService.getAllUsers(page, username, role);
	}
	@PreAuthorize("hasAuthority('admin')")
	@PutMapping("/{id}")
	public User updateUser(@PathVariable String id, @RequestBody User user) {
		return userService.updateUser(id, user);
	}
	@PreAuthorize("hasAuthority('admin')")
	@DeleteMapping("/{id}")
	public void deleteUser(@PathVariable String id) {
		this.userService.deleteUser(id);
	}
}
