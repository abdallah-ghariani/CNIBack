package com.example.demo.servecies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.demo.dto.EmailRequestDto;
import com.example.demo.dto.UserCredentialsDto;
import com.example.demo.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send an email using the configured SMTP server
     */
    public boolean sendPasswordEmail(EmailRequestDto emailRequest) {
        logger.info("Sending email to: {}", emailRequest.getTo());
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getContent(), true); // true indicates HTML content
            
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", emailRequest.getTo());
            return true;
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", emailRequest.getTo(), e);
            return false;
        }
    }
    
    /**
     * Creates a formatted HTML email content with user credentials
     */
    public static String createUserCredentialsEmailContent(User user) {
        return "<html><body>" +
               "<h2>Welcome to the CNI Platform</h2>" +
               "<p>Hello " + user.getUsername() + ",</p>" +
               "<p>Your account has been created. Here are your login credentials:</p>" +
               "<p><strong>Username:</strong> " + user.getUsername() + "<br>" +
               "<strong>Password:</strong> " + user.getPassword() + "</p>" +
               "<p>Please login with these credentials and change your password as soon as possible.</p>" +
               "<p>Regards,<br>" +
               "CNI Team</p>" +
               "</body></html>";
    }
}
