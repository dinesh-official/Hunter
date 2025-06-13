package com.devkng.Hunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "ssh")
public class SshConfig {

    private int port;
    private int asn;
    private Duration duration = new Duration();
    private int minFlowCount;
    private int responseCount;
    private int passwordBasedCondition;
    private Mail mail = new Mail();

    public static class Duration {
        private int hours;

        public int getHours() {
            return hours;
        }

        public void setHours(int hours) {
            this.hours = hours;
        }
    }

    public static class Mail {
        private String type;
        private int skipDaysIfMailed;
        private boolean enabled;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getSkipDaysIfMailed() {
            return skipDaysIfMailed;
        }

        public void setSkipDaysIfMailed(int skipDaysIfMailed) {
            this.skipDaysIfMailed = skipDaysIfMailed;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    // Getters and Setters
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getAsn() {
        return asn;
    }

    public void setAsn(int asn) {
        this.asn = asn;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public int getMinFlowCount() {
        return minFlowCount;
    }

    public void setMinFlowCount(int minFlowCount) {
        this.minFlowCount = minFlowCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public int getPasswordBasedCondition() {
        return passwordBasedCondition;
    }

    public void setPasswordBasedCondition(int passwordBasedCondition) {
        this.passwordBasedCondition = passwordBasedCondition;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }
}
