package com.devkng.Hunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.mail")
public class MailConfig {

    private String host;
    private int port;
    private String username;
    private String password;

    private String to;
    private List<String> cc;  // List to handle multiple CC emails

    private PropertiesProperties properties = new PropertiesProperties();

    public static class PropertiesProperties {
        private boolean smtpAuth;
        private boolean starttlsEnable;
        private boolean starttlsRequired;

        public boolean isSmtpAuth() {
            return smtpAuth;
        }
        public void setSmtpAuth(boolean smtpAuth) {
            this.smtpAuth = smtpAuth;
        }
        public boolean isStarttlsEnable() {
            return starttlsEnable;
        }
        public void setStarttlsEnable(boolean starttlsEnable) {
            this.starttlsEnable = starttlsEnable;
        }
        public boolean isStarttlsRequired() {
            return starttlsRequired;
        }
        public void setStarttlsRequired(boolean starttlsRequired) {
            this.starttlsRequired = starttlsRequired;
        }
    }

    // Getters and Setters
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }

    // Return CC as a comma-separated string
    public String getCc() {
        if (cc == null || cc.isEmpty()) {
            return "";
        }
        return String.join(",", cc);
    }

    // Setter accepts a list as Spring Boot will bind automatically from CSV property
    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public PropertiesProperties getProperties() {
        return properties;
    }
    public void setProperties(PropertiesProperties properties) {
        this.properties = properties;
    }
}
