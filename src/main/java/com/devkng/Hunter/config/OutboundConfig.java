package com.devkng.Hunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "outbound")
public class OutboundConfig {

    private int port;
    private int dstAsn;
    private int srcAsn;
    private Duration duration = new Duration();
    private int responseCount;
    private int minObCount;
    private int minUniqueServerIps;
    private String clientCountry;
    private String serverCountry;
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

    // Getters and setters for all fields

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDstAsn() {
        return dstAsn;
    }

    public void setDstAsn(int dstAsn) {
        this.dstAsn = dstAsn;
    }

    public int getSrcAsn() {
        return srcAsn;
    }

    public void setSrcAsn(int srcAsn) {
        this.srcAsn = srcAsn;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public int getMinObCount() {
        return minObCount;
    }

    public void setMinObCount(int minObCount) {
        this.minObCount = minObCount;
    }

    public int getMinUniqueServerIps() {
        return minUniqueServerIps;
    }

    public void setMinUniqueServerIps(int minUniqueServerIps) {
        this.minUniqueServerIps = minUniqueServerIps;
    }

    public String getClientCountry() {
        return clientCountry;
    }

    public void setClientCountry(String clientCountry) {
        this.clientCountry = clientCountry;
    }

    public String getServerCountry() {
        return serverCountry;
    }

    public void setServerCountry(String serverCountry) {
        this.serverCountry = serverCountry;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }
}
