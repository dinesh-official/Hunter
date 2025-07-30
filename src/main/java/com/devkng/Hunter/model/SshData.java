package com.devkng.Hunter.model;

public class SshData {
    private String srcIp;       // to hold source IP
    private long flowCount;     // to hold count of flows

    // Default constructor
    public SshData() {}

    // Constructor to create object quickly
    public SshData(String srcIp, long flowCount) {
        this.srcIp = srcIp;
        this.flowCount = flowCount;
    }

    // Getter for srcIp
    public String getSrcIp() {
        return srcIp;
    }

    // Setter for srcIp
    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    // Getter for flowCount
    public long getFlowCount() {
        return flowCount;
    }

    // Setter for flowCount
    public void setFlowCount(long flowCount) {
        this.flowCount = flowCount;
    }

    public String toString() {
        return "SshData{" +
                "dstIp='" + srcIp + '\'' +
                ", flowCount=" + flowCount +
                // include other fields as needed
                '}';
    }
}
