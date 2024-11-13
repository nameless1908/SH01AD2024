package com.example.snapheal.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendResetPasswordEmail(String email, String otpToken) throws MessagingException {
        // Prepare the Thymeleaf context and add variables
        Context context = new Context();
        context.setVariable("otpToken", otpToken);

        // Render the HTML template into a String
        String emailContent = templateEngine.process("reset_password_email_template", context);

        // Create and send the email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setTo(email);
        helper.setSubject("Password Reset OTP Code");
        helper.setText(emailContent, true); // true for HTML

        mailSender.send(message);
    }
}
