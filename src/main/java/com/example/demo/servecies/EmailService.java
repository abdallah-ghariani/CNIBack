package com.example.demo.servecies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.EmailRequestDto;
import com.example.demo.dto.UserCredentialsDto;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    /**
     * This method would typically send an email, but for now it just logs the information
     * In a production environment, you would integrate with an email service provider here
     */
    public boolean sendPasswordEmail(EmailRequestDto emailRequest) {
        logger.info("Sending email to: {}", emailRequest.getTo());
        logger.info("Subject: {}", emailRequest.getSubject());
        logger.info("Content: {}", emailRequest.getContent());
        
        // Here you would add the actual email sending logic using your preferred email service
        // For example, using JavaMailSender or a third-party service like SendGrid
        
        return true; // Return true to indicate success
    }
    
    /**
     * Creates a formatted email content with user credentials
     */
    public String createUserCredentialsEmailContent(UserCredentialsDto credentials) {
        return "Hello " + credentials.getUsername() + ",\n\n" +
               "Your account has been created. Here are your login credentials:\n\n" +
               "Username: " + credentials.getUsername() + "\n" +
               "Password: " + credentials.getPassword() + "\n\n" +
               "Please login with these credentials and change your password.\n\n" +
               "Regards,\n" +
               "CNI Team";
    }
}
