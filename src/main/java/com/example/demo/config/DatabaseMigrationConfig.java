package com.example.demo.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import com.example.demo.entity.Api;
import com.example.demo.repository.ApiRepository;

@Configuration
public class DatabaseMigrationConfig implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationConfig.class);
    
    @Autowired
    private ApiRepository apiRepository;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Running database migration for API approval status...");
        
        // Check all existing APIs and set default approval status if null
        List<Api> allApis = apiRepository.findAll();
        int updatedCount = 0;
        
        for (Api api : allApis) {
            if (api.getApprovalStatus() == null) {
                api.setApprovalStatus("approved");
                apiRepository.save(api);
                updatedCount++;
                logger.info("Updated API {} with default approval status 'approved'", api.getId());
            }
        }
        
        logger.info("Database migration completed. Updated {} APIs with default approval status.", updatedCount);
    }
}
