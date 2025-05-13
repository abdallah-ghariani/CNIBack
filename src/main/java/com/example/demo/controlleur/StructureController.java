package com.example.demo.controlleur;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Structure;
import com.example.demo.servecies.StructureService;

@RestController
@RequestMapping("/api/structures")
public class StructureController {

    @Autowired
    private StructureService structureService;
 
  
    @GetMapping("/{id}")
    public Structure getStructureById(@PathVariable String id) {
        return structureService.getStructureById(id);
    }
	@PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}")
    public Structure updateStructure(@PathVariable String id, @RequestBody Structure structure) {
        return structureService.updateStructure(id, structure);
    }
	@PreAuthorize("hasAuthority('admin')")
    @PostMapping
    public Structure addStructure(@RequestBody Structure structure) {
        return structureService.addStructure(structure.getName());
    }

	@PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public void deleteStructure(@PathVariable String id) {
        structureService.deleteStructure(id);
    }

    @GetMapping
    public Page<Structure> getAllStructures(Pageable pageable) {
        return structureService.getAll(pageable);
    }
}
