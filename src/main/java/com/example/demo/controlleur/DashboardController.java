package com.example.demo.controlleur;

import com.example.demo.entity.Api;
import com.example.demo.entity.Secteur;
import com.example.demo.repository.ApiRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.servecies.ApiRequestService;
import com.example.demo.servecies.ApiService;
import com.example.demo.servecies.SecteurService;
import com.example.demo.servecies.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAuthority('admin')")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private SecteurService secteurService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ApiRequestService apiRequestService;
    
    @Autowired
    private ApiRepository apiRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Dashboard statistics endpoint - provides counts for key metrics
     * @return Map containing apiCount, sectorsWithApis, userCount, pendingRequestsCount
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        logger.info("Fetching dashboard statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get total API count directly from repository to avoid filtering issues
            long apiCount = apiRepository.count();
            stats.put("apiCount", apiCount);
            logger.info("Found {} total APIs in database", apiCount);
            
            // Get sectors with APIs
            List<Api> allApis = apiRepository.findAll();
            Set<String> uniqueSectors = new HashSet<>();
            for (Api api : allApis) {
                if (api.getSecteur() != null && !api.getSecteur().isEmpty()) {
                    uniqueSectors.add(api.getSecteur());
                }
            }
            stats.put("sectorsWithApis", uniqueSectors.size());
            logger.info("Found {} unique sectors with APIs", uniqueSectors.size());
            
            // Get total user count directly from repository
            long userCount = userRepository.count();
            stats.put("userCount", userCount);
            logger.info("Found {} total users in database", userCount);
            
            // Get count of pending API requests
            List<?> pendingRequests = apiRequestService.getPendingApiCreationRequests();
            stats.put("pendingRequestsCount", pendingRequests.size());
            logger.info("Found {} pending API requests", pendingRequests.size());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * API Sector Distribution endpoint - provides data for sector distribution chart
     * @return List of sectors with their API counts and percentages
     */
    @GetMapping("/sector-distribution")
    public ResponseEntity<Map<String, Object>> getSectorDistribution() {
        logger.info("Fetching sector distribution data");
        
        try {
            // Get all APIs directly from repository to avoid filtering issues
            List<Api> apis = apiRepository.findAll();
            logger.info("Found {} total APIs for sector distribution", apis.size());
            
            // Get all sectors
            Pageable allItems = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Secteur> sectorsPage = secteurService.getAll(allItems);
            List<Secteur> allSectors = sectorsPage.getContent();
            
            // Create map of sector IDs to sector names
            Map<String, String> sectorIdToName = new HashMap<>();
            for (Secteur sector : allSectors) {
                sectorIdToName.put(sector.getId(), sector.getName());
            }
            
            // Count APIs per sector
            Map<String, Integer> apiCountBySector = new HashMap<>();
            for (Api api : apis) {
                if (api.getSecteur() != null && !api.getSecteur().isEmpty()) {
                    apiCountBySector.put(api.getSecteur(), 
                        apiCountBySector.getOrDefault(api.getSecteur(), 0) + 1);
                }
            }
            
            // Calculate percentages and create response data
            int totalApis = apis.size();
            
            // Define a color palette for the sectors
            String[] colorPalette = {
                "#4285F4", "#EA4335", "#FBBC05", "#34A853", // Google colors
                "#3366CC", "#DC3912", "#FF9900", "#109618", // Classic chart colors
                "#990099", "#0099C6", "#DD4477", "#66AA00", 
                "#B82E2E", "#316395", "#994499", "#22AA99"
            };
            
            List<Map<String, Object>> sectorData = new ArrayList<>();
            int colorIndex = 0;
            
            // Build the sector data list
            for (Map.Entry<String, Integer> entry : apiCountBySector.entrySet()) {
                String sectorId = entry.getKey();
                int apiCount = entry.getValue();
                
                // Calculate percentage
                double percentage = totalApis > 0 ? (apiCount * 100.0) / totalApis : 0;
                
                // Create sector data object
                Map<String, Object> sector = new HashMap<>();
                sector.put("id", sectorId);
                sector.put("name", sectorIdToName.getOrDefault(sectorId, "Unknown Sector"));
                sector.put("apiCount", apiCount);
                sector.put("percentage", Math.round(percentage * 10) / 10.0); // Round to 1 decimal place
                sector.put("color", colorPalette[colorIndex % colorPalette.length]);
                
                sectorData.add(sector);
                colorIndex++;
            }
            
            // Sort sectors by API count (descending)
            sectorData.sort((a, b) -> Integer.compare(
                (int) b.get("apiCount"), 
                (int) a.get("apiCount")
            ));
            
            Map<String, Object> response = new HashMap<>();
            response.put("sectors", sectorData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching sector distribution data", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
