package com.devkng.Hunter.scheduler;


import com.devkng.Hunter.config.*;
import com.devkng.Hunter.mail.TemplateSwitcher;
import com.devkng.Hunter.model.OpenPortsData;
import com.devkng.Hunter.model.SshData;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.service.*;
import com.devkng.Hunter.mail.BulkMailService;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.mail.Template;
import com.devkng.Hunter.utility.Util;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

import java.sql.SQLException;
import java.sql.Statement;
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
    private final BulkMailService bulkMailService ;
    private boolean hasLoggedOnce = false;
    private static final Logger log = LoggerFactory.getLogger(MyScheduler.class);


    public MyScheduler(JavaMailSender mailSender, SshServices sshServices, MailService mailService, DataSource dataSource, OutboundService outboundService, OpenPortsService openPortsService, BandwidthService bandwidthService, OpenPortsService openPortsService1, SshConfig sshConfig, MailConfig mailConfig, OutboundConfig outboundConfig, BandwidthConfig bandwidthConfig, OpenPortsConfig openPortsConfig, BulkMailService bulkMailService) {
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
        this.bulkMailService = bulkMailService;
    }
    @Async
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

    @Async
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

    @Async
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

    private void executeSshCheck() {
        log.info(Util.outSuccess("Executing the SSH method"));

        // Step 1: Fetch already mailed records
        List<Mail> mlist = mailService.fetchMailRecords(
                "", "", "", "", sshConfig.getMail().getType(), sshConfig.getMail().getSkipDaysIfMailed()
        );

        Set<String> alreadyMailedCache = mlist.stream()
                .map(m -> m.getVmIp() + "->" + m.getMailType())
                .collect(Collectors.toSet());

        // Step 2: Fetch SSH suspicious data
        long queryStart = System.currentTimeMillis();
        List<SshData> sshList = sshServices.getSsh(
                sshConfig.getPort(),
                sshConfig.getAsn(),
                sshConfig.getDuration().getHours(),
                sshConfig.getMinFlowCount(),
                sshConfig.getResponseCount(),
                sshConfig.getPasswordBasedCondition(),
                mlist
        );
        long queryEnd = System.currentTimeMillis();
        log.info(Util.outGreen("SSH List Size: " + sshList.size()));
        log.info(Util.outYellow("SSH query took " + (queryEnd - queryStart) + "ms"));

        // Step 3: Prepare thread pool
        ExecutorService executor = Executors.newFixedThreadPool(threadCount); // e.g., 32 core → use 28
        CountDownLatch latch = new CountDownLatch(sshList.size());

        // Step 4: Start bulk SMTP session
        bulkMailService.start();

        // Step 5: Process each SSH record concurrently
        for (SshData sshData : sshList) {
            executor.execute(() -> {
                try {
                    String ip = sshData.getSrcIp();
                    String mailKey = ip + "->" + sshConfig.getMail().getType();

                    // Skip already mailed
                    if (alreadyMailedCache.contains(mailKey)) {
                        log.info("Skipping already mailed IP: {}", ip);
                        return;
                    }

                    // Build mail
                    String subject = Template.getSSHCybSubject(ip);
                    String body = Template.getSSHCybBody(ip, null, String.valueOf(sshConfig.getPort()));

                    boolean start = sshList.indexOf(sshData) == 0;
                    boolean end = sshList.indexOf(sshData) == (sshList.size() - 1);

                    // Send email
                    boolean sent = bulkMailService.sendMail(mailConfig.getTo(), subject, body, mailConfig.getCc());
                    if (!sent) {
                        log.warn("Retrying mail for IP: {}", ip);
                        Thread.sleep(2000);
                        sent = sendMail(mailConfig.getTo(), subject, body, mailConfig.getCc()); // fallback
                    }

                    if (sent) {
                        insertMailRecord("", "", ip, "", sshConfig.getMail().getType());
                        alreadyMailedCache.add(mailKey);
                        log.info(Util.outSuccess("SSH Mail sent to IP: " + ip));
                    } else {
                        log.error("Failed to send mail to IP: {}", ip);
                    }

                } catch (Exception e) {
                    log.error("Exception while processing IP: {}", sshData.getSrcIp(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Step 6: Wait for all to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("SSH detection interrupted", e);
        }

        // Step 7: Cleanup
        bulkMailService.end();
        executor.shutdown();

        log.info(Util.outSuccess("All SSH alert emails processed."));
    }


    private void executeOutboundCheck() {
        out.println("OUTBOUND");
    }

    private void executeBandwidthCheck() {
    }

    // Calculate available processors
    int availableCores = Runtime.getRuntime().availableProcessors();

    // Use 90% of the available cores for thread count
    int threadCount = (int) Math.max(1, Math.floor(availableCores)*1);

    // Initialize the thread pool dynamically
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    // Tune thread pool size as per CPU cores

    private void executeOpenPortsCheck() {
        log.info(Util.outSuccess("Executing the open port method"));

        // Step 1: Pre-fetch already mailed records
        List<Mail> mailList = mailService.fetchMailRecords("", "", "", "", "", openPortsConfig.getMail().getSkipDaysIfMailed());

        // Creating the open port tag eg: 192.168.1.1->OP-3389
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

        log.info(Util.outYellow("OpenPorts query took " + (queryEnd - queryStart) + "ms"));
        log.info(Util.outGreen("Open Ports List Size: " + openPortsList.size()));

        // Step 3: Setup executor and latch
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(openPortsList.size());

        // Authenticate only ONCE outside thread
        bulkMailService.start();

        for (OpenPortsData data : openPortsList) {
            executor.execute(() -> {
                try {
                    String ip = data.getIp();
                    List<Integer> openPorts = data.getOpenPorts();

                    // Check already mailed
                    boolean alreadyMailed = openPorts.stream()
                            .map(port -> ip + "->" + openPortsConfig.getMail().getType() + "-" + port)
                            .anyMatch(alreadyMailedCache::contains);

                    if (alreadyMailed) {
                        log.info("Skipping already mailed IP: {}", ip);
                        return;
                    }

                    // Prepare email content
                    //String subject = Template.getSSHCybSubject(ip);
                    String portList = openPorts.stream().map(String::valueOf).collect(Collectors.joining(","));
                    //String body = Template.getSSHCybBody(ip, null, portList);

                    int primaryPort = openPorts.get(0);
                    TemplateSwitcher.MailTemplate template = TemplateSwitcher.getTemplateForPort(primaryPort, ip,null, portList);

                    boolean sent = bulkMailService.sendMail(mailConfig.getTo(), template.subject, template.body, mailConfig.getCc());


                    //boolean sent = bulkMailService.sendMail(mailConfig.getTo(), subject, body, mailConfig.getCc());
                    if (!sent) {
                        log.warn("Retrying mail for IP: {}", ip);
                        //Thread.sleep(2000);
                        sent = sendMail(mailConfig.getTo(), template.subject, template.body, mailConfig.getCc());
                    }

                    if (sent) {
                        for (Integer port : openPorts) {
                            String type = openPortsConfig.getMail().getType() + "-" + port;
                            insertMailRecord("", "", ip, "", type);
                            //alreadyMailedCache.add(ip + "->" + type);
                            log.info("Mail sent to IP: {} for Port: {}", ip, port);
                        }
                    } else {
                        log.error("Failed to send mail to IP: {}", ip);
                    }

                } catch (Exception e) {
                    log.error("Exception while processing IP: {}", data.getIp(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Open port scanning interrupted", e);
        }

        // ✅ Cleanup after all threads finish
        bulkMailService.end();
        executor.shutdown();
        alreadyMailedCache.clear();
        log.info(Util.outSuccess("All open port emails processed."));
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
