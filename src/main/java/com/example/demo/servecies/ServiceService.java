package com.example.demo.servecies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Service;
import com.example.demo.repository.ServiceRepository;

@org.springframework.stereotype.Service
public class ServiceService {

	@Autowired
	ServiceRepository serviceRepository;

	public Page<Service> getAll(Pageable page) {
		return this.serviceRepository.findAll(page);
	}

	public Service addService(String name, String api) {
		Service service = new Service();
		service.setApi(api);
		service.setName(name);
		return serviceRepository.save(service);
	}

	// Get a service by ID
	public Service getServiceById(String id) {
		return serviceRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id));
	}

	// Update an existing service
	public Service updateService(String id, Service updatedService) {
		if (!id.equals(updatedService.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service ID does not match");
		}
		return serviceRepository.findById(id).map(service -> {
			service.setName(updatedService.getName());
			service.setApi(updatedService.getApi());
			return serviceRepository.save(service);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id));
	}

	// Delete a service by ID
	public void deleteService(String id) {
		if (!serviceRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found with ID: " + id);
		}
		serviceRepository.deleteById(id);
	}

}
