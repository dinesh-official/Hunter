package com.devkng.Hunter.utility;// Required dependencies (Maven):
// - spring-boot-starter-mail
// - com.sun.mail:jakarta.mail
// - com.google.auth:google-auth-library-oauth2-http

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;

@Component
public class GmailOAuth2Mailer {

    private static final String CLIENT_ID = "YOUR_CLIENT_ID";
    private static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET";
    private static final String REFRESH_TOKEN = "YOUR_REFRESH_TOKEN";
    private static final String REDIRECT_URI = "https://developers.google.com/oauthplayground";
    private static final String FROM_EMAIL = "your_email@gmail.com";

    public void sendEmail(String to, String subject, String htmlContent) throws IOException, MessagingException {
        // Get access token
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRefreshToken(REFRESH_TOKEN)
                .setQuotaProjectId("")
                .build();

        AccessToken accessToken = credentials.refreshAccessToken();

        // Set properties for JavaMail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");

        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, accessToken.getTokenValue());
            }
        });

        // Create email message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");
        message.setSentDate(Date.from(Instant.now()));

        // Send email
        Transport.send(message);
    }
}
