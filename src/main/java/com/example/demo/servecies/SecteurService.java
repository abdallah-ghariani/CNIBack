package com.example.demo.servecies;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Secteur;
import com.example.demo.repository.SecteurRepository;

@org.springframework.stereotype.Service
public class SecteurService {

    @Autowired
    private SecteurRepository secteurRepository;

    public Page<Secteur> getAll(Pageable page) {
        return secteurRepository.findAll(page);
    }

    public Secteur addSecteur(String name) {
        Secteur secteur = new Secteur();
        secteur.setName(name);
        return secteurRepository.save(secteur);
    }

    public Secteur getSecteurById(String id) {
        return secteurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur not found with ID: " + id));
    }

    public Secteur updateSecteur(String id, Secteur updatedSecteur) {
        if (!id.equals(updatedSecteur.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secteur ID does not match");
        }
        return secteurRepository.findById(id).map(secteur -> {
            secteur.setName(updatedSecteur.getName());
            return secteurRepository.save(secteur);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur not found with ID: " + id));
    }

    public void deleteSecteur(String id) {
        if (!secteurRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Secteur not found with ID: " + id);
        }
        secteurRepository.deleteById(id);
    }
}
