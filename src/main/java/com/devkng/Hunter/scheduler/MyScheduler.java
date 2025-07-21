package com.devkng.Hunter.scheduler;


import com.devkng.Hunter.config.*;
import com.devkng.Hunter.model.OpenPortsData;
import com.devkng.Hunter.model.SshData;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.service.*;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.Template;
import com.devkng.Hunter.utility.Util;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.out;

// MyScheduler.java
@Component
public class MyScheduler {

    private final JavaMailSender mailSender;
    private final SshServices sshServices;
    private final MailService mailService;
    private final DataSource dataSource;
    private final OutboundService outboundService;
    private final BandwidthService bandwidthService ;
    private final OpenPortsService openPortsService ;
    private final SshConfig sshConfig;
    private final MailConfig mailConfig;
    private final OutboundConfig outboundConfig ;
    private final BandwidthConfig bandwidthConfig ;
    private final OpenPortsConfig openPortsConfig ;
    private boolean hasLoggedOnce = false;
    private static final Logger log = LoggerFactory.getLogger(MyScheduler.class);


    public MyScheduler(JavaMailSender mailSender, SshServices sshServices, MailService mailService, DataSource dataSource, OutboundService outboundService, OpenPortsService openPortsService, BandwidthService bandwidthService, OpenPortsService openPortsService1, SshConfig sshConfig, MailConfig mailConfig, OutboundConfig outboundConfig, BandwidthConfig bandwidthConfig, OpenPortsConfig openPortsConfig) {
        this.mailSender = mailSender;
        this.sshServices = sshServices;
        this.mailService = mailService;
        this.dataSource = dataSource;
        this.outboundService = outboundService;
        this.bandwidthService = bandwidthService;
        this.openPortsService = openPortsService1;
        this.sshConfig = sshConfig;
        this.mailConfig = mailConfig;
        this.outboundConfig = outboundConfig;
        this.bandwidthConfig = bandwidthConfig;
        this.openPortsConfig = openPortsConfig;
    }

    @Scheduled(cron = "${schedule.ssh}")
    public void runSshTask() {
        if (!hasLoggedOnce) {
            logConfig();
            hasLoggedOnce = true;
        }
        if (sshConfig.getMail().isEnabled()) {
            executeSshCheck();
        }
    }

    @Scheduled(cron = "${schedule.outbound}")
    public void runOutboundTask() {
        if (!hasLoggedOnce) {
            logConfig();
            hasLoggedOnce = true;
        }
        if (outboundConfig.getMail().isEnabled()) {
            executeOutboundCheck();
        }
    }

    @Scheduled(cron = "${schedule.bandwidth}")
    public void runBandwidthTask() {
        if (!hasLoggedOnce) {
            logConfig();
            hasLoggedOnce = true;
        }
        if (bandwidthConfig.getMail().isEnabled()) {
            executeBandwidthCheck();
        }
    }


    @Scheduled(cron = "${schedule.openports}")
    public void runOpenPortsTask() {
        if (!hasLoggedOnce) {
            logConfig();
            hasLoggedOnce = true;
        }
        if (openPortsConfig.getMail().isEnabled()) {
            executeOpenPortsCheck();
        }
    }


    private void logConfig() {
        log.info(Util.outSuccess(" Loaded SSH Configuration:"));
        log.info(" • Port: {}", sshConfig.getPort());
        log.info(" • ASN: {}", sshConfig.getAsn());
        log.info(" • Duration (hours): {}", sshConfig.getDuration().getHours());
        log.info(" • Min Flow Count: {}", sshConfig.getMinFlowCount());
        log.info(" • Response Count: {}", sshConfig.getResponseCount());
        log.info(" • Password-based Condition: {}", sshConfig.getPasswordBasedCondition());
        log.info(" • Mail Type: {}", sshConfig.getMail().getType());
        log.info(" • Skip Mail if sent in last {} day(s)", sshConfig.getMail().getSkipDaysIfMailed());
        log.info(" • Mail Enabled: {}", sshConfig.getMail().isEnabled());
        //
        log.info(Util.outGreen(" Loaded Outbound Configuration:"));
        log.info(" • Port: {}", outboundConfig.getPort());
        log.info(" • Destination ASN: {}", outboundConfig.getDstAsn());
        log.info(" • Source ASN: {}", outboundConfig.getSrcAsn());
        log.info(" • Duration (hours): {}", outboundConfig.getDuration().getHours());
        log.info(" • Response Count: {}", outboundConfig.getResponseCount());
        log.info(" • Min OB Count: {}", outboundConfig.getMinObCount());
        log.info(" • Min Unique Server IPs: {}", outboundConfig.getMinUniqueServerIps());
        log.info(" • Client Country: {}", outboundConfig.getClientCountry());
        log.info(" • Server Country: {}", outboundConfig.getServerCountry());
        log.info(" • Mail Type: {}", outboundConfig.getMail().getType());
        log.info(" • Skip Mail if sent in last {} day(s)", outboundConfig.getMail().getSkipDaysIfMailed());
        log.info(" • Mail Enabled: {}", outboundConfig.getMail().isEnabled());
        //
        log.info(Util.outGreen(" Loaded Bandwidth Configuration:"));
        log.info(" • ASN: {}", bandwidthConfig.getDstAsn());
        log.info(" • Duration (hours): {}", bandwidthConfig.getDuration().getHours());
        log.info(" • Threshold (MB): {}", bandwidthConfig.getMBThreshold());
        log.info(" • Limit: {}", bandwidthConfig.getLimit());
        log.info(" • Mail Type: {}", bandwidthConfig.getMail().getType());
        log.info(" • Skip Mail if sent in last {} day(s)", bandwidthConfig.getMail().getSkipDaysIfMailed());
        log.info(" • Mail Enabled: {}", bandwidthConfig.getMail().isEnabled());
        //
        log.info(Util.outGreen(" Loaded Open Ports Configuration:"));
        log.info(" • Destination ASN: {}", openPortsConfig.getDstAsn());
        log.info(" • Interval (hours): {}", openPortsConfig.getIntervalHours());
        log.info(" • Min Request Count: {}", openPortsConfig.getMinRequestCount());
        log.info(" • Result Limit: {}", openPortsConfig.getResponseCount());
        log.info(" • Mail Type: {}", openPortsConfig.getMail().getType());
        log.info(" • Skip Mail if sent in last {} day(s)", openPortsConfig.getMail().getSkipDaysIfMailed());
        log.info(" • Mail Enabled: {}", openPortsConfig.getMail().isEnabled());
    }

    public void executeSshCheck() {
        List<Mail> mlist = null ;
        List<SshData> sshList = null;

        // Fetch previously mailed records once
        mlist = mailService.fetchMailRecords("", "", "", "", sshConfig.getMail().getType(), sshConfig.getMail().getSkipDaysIfMailed());
        sshList = sshServices.getSsh(sshConfig.getPort(), sshConfig.getAsn(), sshConfig.getDuration().getHours(),
                sshConfig.getMinFlowCount(), sshConfig.getResponseCount(), sshConfig.getPasswordBasedCondition(), mlist);
        log.info(Util.outGreen("SSH List Size: " + sshList.size()));


        Set<String> mailedIps = new HashSet<>();

        for (SshData flowData : sshList) {
            String srcIp = flowData.getSrcIp();

            // Skip if already handled in this run
            if (mailedIps.contains(srcIp)) {
                log.info("Duplicate IP in current run, skipping: " + srcIp);
                continue;
            }

            // Check DB history
            List<Mail> mailHistory = mailService.fetchMailRecords("", "", srcIp, "",
                    sshConfig.getMail().getType(), sshConfig.getMail().getSkipDaysIfMailed());
            if (Util.alreadyMailed(mailHistory, srcIp)) {
                log.info("Skipping already mailed IP from DB: " + srcIp);
                continue;
            }

            // Prepare mail content
            String subject = Template.getSSHCybSubject(srcIp);
            String body = Template.getSSHCybBody(srcIp, "", sshConfig.getPort());

            // Send mail (retry once on failure)
            boolean sent = sendMail(mailConfig.getTo(), subject, body ,mailConfig.getCc());
            if (!sent) {
                log.warn("First attempt failed, retrying for IP: " + srcIp);
                try {
                    Thread.sleep(2000); // Small wait before retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sent = sendMail("dineshdb121@gmail.com", subject, body);
            }

            if (sent) {
                insertMailRecord("", "", srcIp, "", sshConfig.getMail().getType());
                mailedIps.add(srcIp);
                log.info(Util.outSuccess("SSH Mail sent to IP: " + srcIp));
            } else {
                log.error("Failed to send mail to IP: " + srcIp + " after 2 attempts");
            }
        }
    }


    private void executeOutboundCheck() {
        out.println("OUTBOUND");
    }

    private void executeBandwidthCheck() {
    }

    private void executeOpenPortsCheck() {
        // Step 1: Fetch open ports data
        List<OpenPortsData> openPortsList = openPortsService.getIncomingData(
                openPortsConfig.getDstAsn(),
                openPortsConfig.getPorts(),
                openPortsConfig.getMinRequestCount(),
                openPortsConfig.getIntervalHours(),
                openPortsConfig.getResponseCount(),
                true
        );
        log.info(Util.outSuccess("Open Ports List Size: " + openPortsList.size()));

        Set<String> mailedIps = new HashSet<>();

        // Step 2: Process each IP entry
        for (OpenPortsData data : openPortsList) {
            String ip = data.getIp();
            List<Integer> openPorts = data.getOpenPorts();
            boolean alreadyMailed = false;

            // Step 3: Check if mail already sent for any of the ports
            for (Integer port : openPorts) {
                String type = openPortsConfig.getMail().getType() + "-" + port;
                List<Mail> mailHistory = mailService.fetchMailRecords("", "", ip, "", type, openPortsConfig.getMail().getSkipDaysIfMailed());
                if (!mailHistory.isEmpty()) {
                    alreadyMailed = true;
                    log.info("Skipping IP: " + ip + " for Port: " + port + " (Already mailed recently) " + type);
                    break;
                }
            }

            if (alreadyMailed) continue;

            // Step 4: Prepare mail content
            String subject = Template.getSSHCybSubject(ip);
            String body = Template.getSSHCybBody(ip, null, openPorts.size());

            // Step 5: Send mail (with retry)
            boolean sent = sendMail(mailConfig.getTo(), subject, body, mailConfig.getCc());
            if (!sent) {
                log.warn("First attempt failed, retrying for IP: " + ip);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sent = sendMail(mailConfig.getTo(), subject, body, mailConfig.getCc());
            }

            // Step 6: Handle success or failure
            if (sent) {
                for (Integer port : openPorts) {
                    String type = openPortsConfig.getMail().getType() + "-" + port;
                    insertMailRecord("", "", ip, "", type);
                    log.info("Open port Mail sent to IP: " + ip + " (Port: " + port + ") " + type);
                }
                mailedIps.add(ip);
            } else {
                log.error("Failed to send mail to IP: " + ip + " after 2 attempts");
            }
        }
    }





    public Boolean sendMail(String to, String subject, String body, String... cc) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("dineshkumar.s@e2entworks.com");
            helper.setTo(to);

            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }

            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info(Util.outSuccess("send email: " + to));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(Util.outRed("Failed to send email: " + e.getMessage()));
            return false;
        }
    }

    // Insert a mail record into DB
    public void insertMailRecord(String vmId, String vmName, String vmIp, String vmOwner, String mailType) {
        String sql = Query.insertMail(vmId, vmName, vmIp, vmOwner, mailType);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            out.println(Util.outYellow("Data inserted"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
