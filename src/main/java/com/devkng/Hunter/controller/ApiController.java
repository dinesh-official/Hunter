package com.devkng.Hunter.controller;


import com.devkng.Hunter.model.*;
import com.devkng.Hunter.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final SshServices flowDataService;
    private final OutboundService outboundService ;
    private final MailService mailService;
    private final BandwidthService bandwidthService;
    private final OpenPortsService openPortsService;
    private List<Mail> mailList;

    public ApiController(SshServices flowDataService, OutboundService outboundService, MailService mailService, BandwidthService bandwidthService, OpenPortsService openPortsService) {
        this.flowDataService = flowDataService;
        this.outboundService = outboundService;
        this.mailService = mailService;
        this.bandwidthService = bandwidthService;
        this.openPortsService = openPortsService;
    }


    @GetMapping("/ssh")
    public List<SshData> getFlowData(
            @RequestParam(name = "p", defaultValue = "22") int ipDstPort,
            @RequestParam(name = "a", defaultValue = "132420") int dstAsn,
            @RequestParam(name = "h", defaultValue = "600") int intervalHour,
            @RequestParam(name = "fc", defaultValue = "1") int flowCountThreshold,
            @RequestParam(name = "rc", defaultValue = "2") int maxResults,
            @RequestParam(name = "np", defaultValue = "2") int noPasswordFlag

    ) {
        return flowDataService.getSsh(ipDstPort, dstAsn, intervalHour, flowCountThreshold, maxResults, noPasswordFlag,mailList);
    }

    @GetMapping("/ob")
    public List<OutBoundData> getFlowData(
            @RequestParam(name = "p", defaultValue = "0") int ipDstPort,
            @RequestParam(name = "sa", defaultValue = "132420") int dstAsn,
            @RequestParam(name = "ca", defaultValue = "132420") int srcAsn,
            @RequestParam(name = "h", defaultValue = "600") int intervalHour,
            @RequestParam(name = "cctry", defaultValue = "IN") String clientCountry,
            @RequestParam(name = "sctry", defaultValue = "IN") String serverCountry,
            @RequestParam(name = "rc", defaultValue = "10") int responseCount,
            @RequestParam(name = "obc", defaultValue = "0") int minObCount,
            @RequestParam(name = "usip", defaultValue = "0") int minUniqueServerIps
    ) {
        return outboundService.getOutboundData(ipDstPort,dstAsn,srcAsn,intervalHour,clientCountry,serverCountry,responseCount,minObCount,minUniqueServerIps);
    }

    @GetMapping("/bw")
    public List<BandwidthData> getBandwidthData(
            @RequestParam(name = "h", defaultValue = "600") int intervalHours,
            @RequestParam(name = "sa", defaultValue = "132420") int dstAsn,
            @RequestParam(name = "mmb", defaultValue = "524288000") long mBThreshold,
            @RequestParam(name = "rc", defaultValue = "10") int limit
    ) {
        return bandwidthService.getBandwidthData(intervalHours, dstAsn, mBThreshold, limit);
    }

    @GetMapping("/op")
    public List<OpenPortsData> getIncomingTraffic(
            @RequestParam(defaultValue = "132420") int dstAsn,
            @RequestParam(required = false) List<Integer> dstPorts,
            @RequestParam(defaultValue = "0") int minRequestCount,
            @RequestParam(defaultValue = "24") int intervalHours,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean notMailed
    ) {
        return  openPortsService.getIncomingData(dstAsn, dstPorts, minRequestCount, intervalHours, limit,notMailed);
    }



    @GetMapping("/mail")
    public List<Mail> getMailRecords(
            @RequestParam(name = "vi", required = false) String vmId,
            @RequestParam(name = "vo", required = false) String vmOwner,
            @RequestParam(name = "vip", required = false) String vmIp,
            @RequestParam(name = "vn", required = false) String vmName,
            @RequestParam(name = "mt", required = false) String mailType,
            @RequestParam(name = "d", required = false, defaultValue = "1") int lastDays
    ) {
        return mailService.fetchMailRecords(vmId, vmOwner, vmIp, vmName, mailType, lastDays);
    }

}
