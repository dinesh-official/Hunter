package com.devkng.Hunter.mail;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class BulkMailService {
    private final JavaMailSenderImpl mailSender;
    private Session session;
    private Transport transport;
    private static final Logger log = LoggerFactory.getLogger(BulkMailService.class);

    public BulkMailService(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    // Called once before the bulk send operation starts
    public synchronized void start() {
        try {
            Properties props = mailSender.getJavaMailProperties();
            session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailSender.getUsername(), mailSender.getPassword());
                }
            });
            transport = session.getTransport("smtp");
            transport.connect(mailSender.getHost(), mailSender.getPort(), mailSender.getUsername(), mailSender.getPassword());
            log.info("Authenticated SMTP session started.");
        } catch (Exception e) {
            log.error("Failed to initialize SMTP session in start()", e);
        }
    }

    // Called per mail send
    public synchronized boolean sendMail(String to, String subject, String body, String... cc) {
        try {
            if (transport == null || !transport.isConnected()) {
                log.warn("SMTP transport is not connected. Call start() before sending mails.");
                return false;
            }

            MimeMessage message = new MimeMessage(session);
            assert mailSender.getUsername() != null;
            message.setFrom(new InternetAddress(mailSender.getUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            if (cc != null && cc.length > 0) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(String.join(",", cc)));
            }
            message.setSubject(subject);
            message.setContent(body, "text/html");

            transport.sendMessage(message, message.getAllRecipients());
            log.info("Mail sent to: " + to);
            return true;

        } catch (Exception e) {
            log.error("Failed to send mail to: " + to, e);
            return false;
        }
    }

    // Called once after all mails are sent
    public synchronized void end() {
        try {
            if (transport != null && transport.isConnected()) {
                transport.close();
                log.info("SMTP transport closed.");
            }
            session = null;
        } catch (Exception e) {
            log.warn("Error while closing transport in end()", e);
        } finally {
            log.info("Bulk mail session cleanup complete.");
        }
    }
}
