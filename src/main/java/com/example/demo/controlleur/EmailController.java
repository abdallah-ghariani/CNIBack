package com.example.demo.controlleur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.EmailRequestDto;
import com.example.demo.servecies.EmailService;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Endpoint to send an email with password for newly created users
     */
    @PostMapping("/send-password")
    public ResponseEntity<Boolean> sendPasswordEmail(@RequestBody EmailRequestDto emailRequest) {
        logger.info("Received request to send password email to: {}", emailRequest.getTo());
        
        boolean sent = emailService.sendPasswordEmail(emailRequest);
        
        if (sent) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.internalServerError().body(false);
        }
    }
}
