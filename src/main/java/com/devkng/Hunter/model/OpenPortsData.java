package com.devkng.Hunter.model;

import java.util.ArrayList;
import java.util.List;

public class OpenPortsData {
    private String ip;
    private int requestCount;
    private int uniqueSourceIps;
    private List<Integer> openPorts = new ArrayList<>();

    public OpenPortsData() {}

    public OpenPortsData(String ip, int requestCount, int uniqueSourceIps) {
        this.ip = ip;
        this.requestCount = requestCount;
        this.uniqueSourceIps = uniqueSourceIps;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getUniqueSourceIps() {
        return uniqueSourceIps;
    }

    public void setUniqueSourceIps(int uniqueSourceIps) {
        this.uniqueSourceIps = uniqueSourceIps;
    }

    public List<Integer> getOpenPorts() {
        return openPorts;
    }

    public void setOpenPorts(List<Integer> openPorts) {
        this.openPorts = openPorts;
    }
}
