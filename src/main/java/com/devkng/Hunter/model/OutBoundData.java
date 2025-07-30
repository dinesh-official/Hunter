package com.devkng.Hunter.model;

import java.util.List;

public class OutBoundData {
    private String clientIp;
    private int obCount;
    private int uniqueServerIps;
    private List<Integer> destinationPorts;

    // Getters and Setters

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public int getObCount() {
        return obCount;
    }

    public void setObCount(int obCount) {
        this.obCount = obCount;
    }

    public int getUniqueServerIps() {
        return uniqueServerIps;
    }

    public void setUniqueServerIps(int uniqueServerIps) {
        this.uniqueServerIps = uniqueServerIps;
    }

    public List<Integer> getDestinationPorts() {
        return destinationPorts;
    }

    public void setDestinationPorts(List<Integer> destinationPorts) {
        this.destinationPorts = destinationPorts;
    }

    @Override
    public String toString() {
        return "OutBoundData{" +
                "clientIp='" + clientIp + '\'' +
                ", obCount=" + obCount +
                ", uniqueServerIps=" + uniqueServerIps +
                ", destinationPorts=" + destinationPorts +
                '}';
    }
}
