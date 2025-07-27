package com.devkng.Hunter.model;

import java.time.LocalDateTime;


public class MailData {

    private Long id;

    private LocalDateTime createdAd;
    private String vmId;
    private String vmName;
    private String vmIp;
    private String vmOwner;
    private String mailType;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    // Added setter for id
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAd() {
        return createdAd;
    }

    public void setCreatedAd(LocalDateTime createdAd) {
        this.createdAd = createdAd;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getVmOwner() {
        return vmOwner;
    }

    public void setVmOwner(String vmOwner) {
        this.vmOwner = vmOwner;
    }

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }
}
