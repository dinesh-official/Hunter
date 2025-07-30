package com.devkng.Hunter.model;

public class BandwidthData {
    private String srcIp;
    private int dstAsn;
    private double totalGb;

    public String getSrcIp() { return srcIp; }
    public void setSrcIp(String srcIp) { this.srcIp = srcIp; }

    public int getDstAsn() { return dstAsn; }
    public void setDstAsn(int dstAsn) { this.dstAsn = dstAsn; }

    public double getTotalGb() { return totalGb; }
    public void setTotalGb(double totalGb) { this.totalGb = totalGb; }
}
