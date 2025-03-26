package com.example.demo.servecies;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Service;
import com.example.demo.entity.Structure;
import com.example.demo.repository.StructureRepository;

@org.springframework.stereotype.Service
public class StructureService {

    @Autowired
    private StructureRepository structureRepository;

    // Get all structures with pagination
    public Page<Structure> getAll(Pageable page) {
        return structureRepository.findAll(page);
    }
  
  
    public Structure addStructure( String name) {
        Structure structure = new Structure();
        structure.setName(name);
     
        return structureRepository.save(structure);
    }

    // Get a structure by ID
    public Structure getStructureById(String id) {
        return structureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Structure not found with ID: " + id));
    }

    // Update an existing structure
    public Structure updateStructure(String id, Structure updatedStructure) {
        if (!id.equals(updatedStructure.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Structure ID does not match");
        }
        return structureRepository.findById(id).map(structure -> {
            structure.setName(updatedStructure.getName());
            return structureRepository.save(structure);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Structure not found with ID: " + id));
    }

    // Delete a structure by ID
    public void deleteStructure(String id) {
        if (!structureRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Structure not found with ID: " + id);
        }
        structureRepository.deleteById(id);
    }
}
