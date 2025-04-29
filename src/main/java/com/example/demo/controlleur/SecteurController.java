package com.example.demo.controlleur;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.SecteurAddDto;
import com.example.demo.entity.Secteur;
import com.example.demo.servecies.SecteurService;

@RestController
@RequestMapping("/api/secteurs")
public class SecteurController {

    @Autowired
    private SecteurService secteurService;
    
	@PreAuthorize("hasAuthority('admin')")
    @GetMapping
    public Page<Secteur> getAllSecteurs(Pageable pageable) {
        return secteurService.getAll(pageable);
    }
	@PreAuthorize("hasAuthority('admin')")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Secteur addSecteur(@RequestBody SecteurAddDto secteur) {
        return secteurService.addSecteur(secteur.getName());
    }
	@PreAuthorize("hasAuthority('admin')")

    @GetMapping("/{id}")
    public Secteur getSecteurById(@PathVariable String id) {
        return secteurService.getSecteurById(id);
    }
	@PreAuthorize("hasAuthority('admin')")

    @PutMapping("/{id}")
    public Secteur updateSecteur(@PathVariable String id, @RequestBody Secteur updatedSecteur) {
        return secteurService.updateSecteur(id, updatedSecteur);
    }
	@PreAuthorize("hasAuthority('admin')")

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecteur(@PathVariable String id) {
        secteurService.deleteSecteur(id);
    }
}

