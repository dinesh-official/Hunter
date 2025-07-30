package com.devkng.Hunter.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "openports")
public class OpenPortsConfig {

    private int dstAsn;
    private int intervalHours;
    private int minRequestCount;
    private int responseCount;
    private List<Integer> ports = new ArrayList<>();
    private Mail mail = new Mail();

    @PostConstruct
    public void validate() {
        if (responseCount < 1) {
            responseCount = 10;  // default fallback
        }
        if (ports == null) {
            ports = new ArrayList<>();
        }
    }

    // Getters and Setters

    public int getDstAsn() {
        return dstAsn;
    }

    public void setDstAsn(int dstAsn) {
        this.dstAsn = dstAsn;
    }

    public int getIntervalHours() {
        return intervalHours;
    }

    public void setIntervalHours(int intervalHours) {
        this.intervalHours = intervalHours;
    }

    public int getMinRequestCount() {
        return minRequestCount;
    }

    public void setMinRequestCount(int minRequestCount) {
        this.minRequestCount = minRequestCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    // ✅ Nested Mail Class
    public static class Mail {
        private String type;
        private boolean enabled;
        private int skipDaysIfMailed;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getSkipDaysIfMailed() {
            return skipDaysIfMailed;
        }

        public void setSkipDaysIfMailed(int skipDaysIfMailed) {
            this.skipDaysIfMailed = skipDaysIfMailed;
        }
    }
}
