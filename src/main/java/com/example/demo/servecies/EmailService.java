package com.example.demo.servecies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.demo.dto.EmailRequestDto;
import com.example.demo.dto.UserCredentialsDto;

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
    /**
     * Send a notification when an API access request is approved
     * @param toEmail Recipient email address
     * @param apiName Name of the API that was approved
     * @param feedback Optional feedback from the approver
     */
    public void sendRequestApprovalNotification(String toEmail, String apiName, String feedback) {
        String subject = "Your API Access Request Has Been Approved";
        String content = createApprovalEmailContent(apiName, feedback);
        
        EmailRequestDto emailRequest = new EmailRequestDto();
        emailRequest.setTo(toEmail);
        emailRequest.setSubject(subject);
        emailRequest.setContent(content);
        
        sendEmail(emailRequest);
    }
    
    /**
     * Send a notification when an API access request is rejected
     * @param toEmail Recipient email address
     * @param apiName Name of the API that was rejected
     * @param feedback Optional feedback from the rejector
     */
    public void sendRequestRejectionNotification(String toEmail, String apiName, String feedback) {
        String subject = "Your API Access Request Has Been Rejected";
        String content = createRejectionEmailContent(apiName, feedback);
        
        EmailRequestDto emailRequest = new EmailRequestDto();
        emailRequest.setTo(toEmail);
        emailRequest.setSubject(subject);
        emailRequest.setContent(content);
        
        sendEmail(emailRequest);
    }
    
    /**
     * Send an email using the provided EmailRequestDto
     */
    private boolean sendEmail(EmailRequestDto emailRequest) {
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
     * Creates HTML content for the approval email
     */
    private String createApprovalEmailContent(String apiName, String feedback) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>API Access Request Approved</h2>");
        content.append("<p>Your request to access the API <strong>")
              .append(apiName).append("</strong> has been approved.</p>");
        
        if (feedback != null && !feedback.trim().isEmpty()) {
            content.append("<h3>Feedback from the approver:</h3>");
            content.append("<p>").append(feedback).append("</p>");
        }
        
        content.append("<p>You can now start using the API with your credentials.</p>");
        content.append("<p>Best regards,<br>The API Team</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    /**
     * Creates HTML content for the rejection email
     */
    private String createRejectionEmailContent(String apiName, String feedback) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>API Access Request Rejected</h2>");
        content.append("<p>We regret to inform you that your request to access the API <strong>")
              .append(apiName).append("</strong> has been rejected.</p>");
        
        if (feedback != null && !feedback.trim().isEmpty()) {
            content.append("<h3>Feedback from the reviewer:</h3>");
            content.append("<p>").append(feedback).append("</p>");
        }
        
        content.append("<p>If you believe this is a mistake, please contact support.</p>");
        content.append("<p>Best regards,<br>The API Team</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    /**
     * Creates a formatted HTML email content with user credentials
     */
    public String createUserCredentialsEmailContent(UserCredentialsDto credentials) {
        return "<html><body>" +
               "<h2>Welcome to the CNI Platform</h2>" +
               "<p>Hello " + credentials.getUsername() + ",</p>" +
               "<p>Your account has been created. Here are your login credentials:</p>" +
               "<p><strong>Username:</strong> " + credentials.getUsername() + "<br>" +
               "<strong>Password:</strong> " + credentials.getPassword() + "</p>" +
               "<p>Please login with these credentials and change your password as soon as possible.</p>" +
               "<p>Regards,<br>" +
               "CNI Team</p>" +
               "</body></html>";
    }
}
