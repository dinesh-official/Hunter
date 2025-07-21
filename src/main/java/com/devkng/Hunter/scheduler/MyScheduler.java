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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
            String body = Template.getSSHCybBody(srcIp, "", String.valueOf(sshConfig.getPort()));

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

    // Calculate available processors
    int availableCores = Runtime.getRuntime().availableProcessors();

    // Use 90% of the available cores for thread count
    int threadCount = (int) Math.max(1, Math.floor(availableCores * 0.9));

    // Initialize the thread pool dynamically
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    // Tune thread pool size as per CPU cores

    private void executeOpenPortsCheck() {
        log.info(Util.outSuccess("🔍 Executing the open port method"));
        log.info("⚙️ Initializing ExecutorService with {} threads (90% of {} cores)", threadCount, availableCores);
        // Step 1: Pre-fetch mail records to avoid repeated DB calls
        List<Mail> mailList = mailService.fetchMailRecords("", "", "", "", "", openPortsConfig.getMail().getSkipDaysIfMailed());

        Set<String> alreadyMailedCache = mailList.stream()
                .map(m -> m.getVmIp() + "->" + m.getMailType())
                .collect(Collectors.toSet());

        long queryStart = System.currentTimeMillis();

        // Step 2: Fetch open ports data
        List<OpenPortsData> openPortsList = openPortsService.getIncomingData(
                openPortsConfig.getDstAsn(),
                openPortsConfig.getPorts(),
                openPortsConfig.getMinRequestCount(),
                openPortsConfig.getIntervalHours(),
                openPortsConfig.getResponseCount(),
                true
        );
        long queryEnd = System.currentTimeMillis();
        log.info(Util.outSuccess("OpenPorts query took " + (queryEnd - queryStart) + "ms"));
        log.info(Util.outSuccess("Open Ports List Size: " + openPortsList.size()));

        CountDownLatch latch = new CountDownLatch(openPortsList.size());

        for (OpenPortsData data : openPortsList) {
            executor.execute(() -> {
                try {
                    String ip = data.getIp();
                    List<Integer> openPorts = data.getOpenPorts();

                    // Check if any port has already been mailed
                    boolean alreadyMailed = openPorts.stream()
                            .map(port -> ip + "->" + openPortsConfig.getMail().getType() + "-" + port)
                            .anyMatch(alreadyMailedCache::contains);

                    if (alreadyMailed) {
                        log.info("Skipping already mailed IP: " + ip);
                        return;
                    }

                    // Prepare subject & body
                    String subject = Template.getSSHCybSubject(ip);
                    String portList = openPorts.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
                    String body = Template.getSSHCybBody(ip, null, openPorts.toString());

                    // Send mail with retry
                    boolean sent = sendMail(mailConfig.getTo(), subject, body, mailConfig.getCc());
                    if (!sent) {
                        log.warn("First attempt failed for IP: " + ip + " - Retrying...");
                        Thread.sleep(2000);
                        sent = sendMail(mailConfig.getTo(), subject, body, mailConfig.getCc());
                    }

                    if (sent) {
                        for (Integer port : openPorts) {
                            String type = openPortsConfig.getMail().getType() + "-" + port;
                            insertMailRecord("", "", ip, "", type);
                            alreadyMailedCache.add(ip + "-" + type);
                            log.info("Mail sent to IP: " + ip + " for Port: " + port);
                        }
                    } else {
                        log.error("Mail failed after retries for IP: " + ip);
                    }

                } catch (Exception e) {
                    log.error("Exception while processing IP: " + data.getIp(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            log.info(Util.outSuccess("All open port checks completed."));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Open port scanning interrupted", e);
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
