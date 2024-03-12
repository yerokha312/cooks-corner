package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.service.interfaces.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class MailService implements NotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine engine;
    @Value("${GMAIL_USERNAME}")
    private String from;

    @Autowired
    public MailService(JavaMailSender mailSender, SpringTemplateEngine engine) {
        this.mailSender = mailSender;
        this.engine = engine;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send an email");
        }
    }

    public void sendConfirmationEmail(String to, String confirmationUrl) {
        Context context = new Context();
        context.setVariable("confirmationUrl", confirmationUrl);

        String emailBody = engine.process(confirmationUrl
                .contains("confirmation") ? "confirmation_email" : "confirmation_password", context);

        send(to, confirmationUrl
                .contains("confirmation") ? "Email confirmation" : "Password reset", emailBody);
    }
}

