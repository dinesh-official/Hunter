package com.devkng.Hunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bandwidth")
public class BandwidthConfig {

    private int dstAsn;
    private Duration duration = new Duration();
    private long mBThreshold;
    private int minGb;
    private int responseCount;
    private Mail mail = new Mail();

    // ✅ Getters and Setters

    public int getDstAsn() {
        return dstAsn;
    }

    public void setDstAsn(int dstAsn) {
        this.dstAsn = dstAsn;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public long getMBThreshold() {
        return mBThreshold;
    }

    public void setMBThreshold(long mBThreshold) {
        this.mBThreshold = mBThreshold;
    }

    public int getMinGb() {
        return minGb;
    }

    public void setMinGb(int minGb) {
        this.minGb = minGb;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    // ✅ Nested Duration class
    public static class Duration {
        private int hours;

        public int getHours() {
            return hours;
        }

        public void setHours(int hours) {
            this.hours = hours;
        }
    }

    // ✅ Nested Mail class
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
