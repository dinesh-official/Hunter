package com.devkng.Hunter.controller;


import com.devkng.Hunter.config.ObConfig;
import com.devkng.Hunter.model.SshData;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.model.OutBoundData;
import com.devkng.Hunter.service.MailService;
import com.devkng.Hunter.service.OutboundService;
import com.devkng.Hunter.service.SshServices;
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
    private List<Mail> mailList;

    public ApiController(SshServices flowDataService, ObConfig obConfig, OutBoundData obData, OutboundService outboundService, MailService mailService) {
        this.flowDataService = flowDataService;
        this.outboundService = outboundService;
        this.mailService = mailService;
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
            @RequestParam(name = "p", defaultValue = "22") int ipDstPort,
            @RequestParam(name = "srv_asn", defaultValue = "132420") int dstAsn,
            @RequestParam(name = "cli_asn", defaultValue = "132420") int srcAsn,
            @RequestParam(name = "hrs", defaultValue = "600") int intervalHour,
            @RequestParam(name = "clt_ctry", defaultValue = "IN") String clientCountry,
            @RequestParam(name = "srv_ctry", defaultValue = "US") String serverCountry,
            @RequestParam(name = "rc", defaultValue = "10") int responseCount
    ) {
        return outboundService.getOutboundData(ipDstPort,dstAsn,srcAsn,intervalHour,clientCountry,serverCountry,responseCount);
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
