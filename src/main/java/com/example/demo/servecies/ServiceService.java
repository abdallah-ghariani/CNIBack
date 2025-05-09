package com.example.demo.servecies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Api;
import com.example.demo.entity.Service;
import com.example.demo.repository.ApiRepository;
import com.example.demo.repository.ServiceRepository;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

	@Autowired
	ServiceRepository serviceRepository;
	
	@Autowired
	ApiRepository apiRepository;

	/**
	 * Get all services with pagination
	 */
	public Page<Service> getAll(Pageable page) {
		return this.serviceRepository.findAll(page);
	}

	/**
	 * Create a new service with name and description
	 */
	public Service addService(String name, String description) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUserId = authentication.getName();
		
		Service service = new Service(name, description);
		service.setCreatedBy(currentUserId);
		return serviceRepository.save(service);
	}
	
	/**
	 * Create a new service from a complete service object
	 */
	public Service createService(Service service) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUserId = authentication.getName();
		
		service.setCreatedBy(currentUserId);
		return serviceRepository.save(service);
	}

	/**
	 * Get a service by ID
	 */
	public Service getServiceById(String id) {
		return serviceRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id));
	}

	/**
	 * Update an existing service
	 */
	public Service updateService(String id, Service updatedService) {
		if (!id.equals(updatedService.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service ID does not match");
		}
		return serviceRepository.findById(id).map(service -> {
			service.setName(updatedService.getName());
			service.setDescription(updatedService.getDescription());
			// Keep existing relationships and other fields that shouldn't be updated directly
			return serviceRepository.save(service);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id));
	}

	/**
	 * Delete a service by ID
	 */
	public void deleteService(String id) {
		if (!serviceRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id);
		}
		serviceRepository.deleteById(id);
	}
	
	/**
	 * Add an API to a service
	 */
	public Service addApiToService(String serviceId, String apiId) {
		Service service = getServiceById(serviceId);
		Api api = apiRepository.findById(apiId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API not found with ID: " + apiId));
		
		List<Api> apis = service.getApis();
		if (apis == null) {
			apis = new ArrayList<>();
		}
		
		if (!apis.contains(api)) {
			apis.add(api);
			service.setApis(apis);
			return serviceRepository.save(service);
		}
		
		return service;
	}
	
	/**
	 * Remove an API from a service
	 */
	public Service removeApiFromService(String serviceId, String apiId) {
		Service service = getServiceById(serviceId);
		Api api = apiRepository.findById(apiId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API not found with ID: " + apiId));
		
		List<Api> apis = service.getApis();
		if (apis != null && apis.remove(api)) {
			service.setApis(apis);
			return serviceRepository.save(service);
		}
		
		return service;
	}
}
