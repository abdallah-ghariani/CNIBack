package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.example.demo.entity.Secteur;
import com.example.demo.entity.Structure;
import com.example.demo.entity.User;
import com.example.demo.repository.SecteurRepository;
import com.example.demo.repository.StructureRepository;
import com.mongodb.client.result.UpdateResult;

import java.util.List;

/**
 * Configuration to automatically set secteur and structure for users that don't have them
 */
@Configuration
public class UserStructureMigrationConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(UserStructureMigrationConfig.class);
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private SecteurRepository secteurRepository;
    
    @Autowired
    private StructureRepository structureRepository;
    
    @Bean
    public CommandLineRunner userStructureMigration() {
        return args -> {
            logger.info("Starting user secteur and structure migration...");
            
            // Find a default secteur and structure to use
            Secteur defaultSecteur = getDefaultSecteur();
            Structure defaultStructure = getDefaultStructure();
            
            if (defaultSecteur == null || defaultStructure == null) {
                logger.warn("Could not find default secteur or structure for migration. No migration performed.");
                return;
            }
            
            // Update users with missing secteur
            Query missingSecteurQuery = new Query(Criteria.where("secteur").exists(false));
            Update secteurUpdate = new Update().set("secteur", defaultSecteur);
            UpdateResult secteurResult = mongoTemplate.updateMulti(missingSecteurQuery, secteurUpdate, User.class);
            
            // Update users with missing structure
            Query missingStructureQuery = new Query(Criteria.where("structure").exists(false));
            Update structureUpdate = new Update().set("structure", defaultStructure);
            UpdateResult structureResult = mongoTemplate.updateMulti(missingStructureQuery, structureUpdate, User.class);
            
            logger.info("User migration completed. Updated {} users with default secteur and {} users with default structure", 
                    secteurResult.getModifiedCount(), structureResult.getModifiedCount());
        };
    }
    
    /**
     * Get a default secteur to use for migration
     */
    private Secteur getDefaultSecteur() {
        // First try to find an existing secteur
        List<Secteur> secteurs = secteurRepository.findAll();
        if (!secteurs.isEmpty()) {
            return secteurs.get(0);
        }
        
        // If no secteurs exist, create a default one
        logger.info("No existing secteurs found, creating a default one");
        Secteur defaultSecteur = new Secteur();
        defaultSecteur.setName("Default Secteur");
        return secteurRepository.save(defaultSecteur);
    }
    
    /**
     * Get a default structure to use for migration
     */
    private Structure getDefaultStructure() {
        // First try to find an existing structure
        List<Structure> structures = structureRepository.findAll();
        if (!structures.isEmpty()) {
            return structures.get(0);
        }
        
        // If no structures exist, create a default one
        logger.info("No existing structures found, creating a default one");
        Secteur defaultSecteur = getDefaultSecteur();
        
        Structure defaultStructure = new Structure();
        defaultStructure.setName("Default Structure");
        defaultStructure.setSecteur(defaultSecteur);
        return structureRepository.save(defaultStructure);
    }
}
