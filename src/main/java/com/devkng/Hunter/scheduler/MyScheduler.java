package com.devkng.Hunter.scheduler;


import com.devkng.Hunter.config.MailConfig;
import com.devkng.Hunter.config.OutboundConfig;
import com.devkng.Hunter.config.SshConfig;
import com.devkng.Hunter.model.SshData;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.service.MailService;
import com.devkng.Hunter.service.SshServices;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.Template;
import com.devkng.Hunter.utility.Util;
import jakarta.mail.internet.MimeMessage;
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
    private final SshConfig sshConfig;
    private final MailConfig mailConfig;
    private final OutboundConfig outboundConfig ;
    private boolean hasLoggedOnce = false;
    private boolean hasLoggedOutbound = false ;
    // ANSI color codes




    public MyScheduler(JavaMailSender mailSender, SshServices sshServices, MailService mailService, DataSource dataSource, SshConfig sshConfig, MailConfig mailConfig, OutboundConfig outboundConfig) {
        this.mailSender = mailSender;
        this.sshServices = sshServices;
        this.mailService = mailService;
        this.dataSource = dataSource;
        this.sshConfig = sshConfig;
        this.mailConfig = mailConfig;
        this.outboundConfig = outboundConfig;
    }

    @Scheduled(cron = "${schedule.ssh}")
    public void runSshTask() {
        if (!hasLoggedOnce) {
            logSshConfig();
            hasLoggedOnce = true;
        }
        if (sshConfig.getMail().isEnabled()) {
            executeSshCheck();
        }
    }

    @Scheduled(cron = "${schedule.outbound}")
    public void runOutboundTask() {
        if (!hasLoggedOutbound) {
            logOutboundConfig();
            hasLoggedOutbound = true;
        }
        if (outboundConfig.getMail().isEnabled()) {
            executeOutboundCheck();
        }
    }

    private void logSshConfig() {
        out.println(" Loaded SSH Configuration:");
        out.println(" • Port: " + sshConfig.getPort());
        out.println(" • ASN: " + sshConfig.getAsn());
        out.println(" • Duration (hours): " + sshConfig.getDuration().getHours());
        out.println(" • Min Flow Count: " + sshConfig.getMinFlowCount());
        out.println(" • Response Count: " + sshConfig.getResponseCount());
        out.println(" • Password-based Condition: " + sshConfig.getPasswordBasedCondition());
        out.println(" • Mail Type: " + sshConfig.getMail().getType());
        out.println(" • Skip Mail if sent in last " + sshConfig.getMail().getSkipDaysIfMailed() + " day(s)");
        out.println(" • Mail Enabled: " + sshConfig.getMail().isEnabled());
    }

    private void logOutboundConfig() {
        out.println(" Loaded OUTBOUND Configuration:");
        out.println(" • Port: " + outboundConfig.getPort());
        out.println(" • Destination ASN: " + outboundConfig.getDstAsn());
        out.println(" • Source ASN: " + outboundConfig.getSrcAsn());
        out.println(" • Duration (hours): " + outboundConfig.getDuration().getHours());
        out.println(" • Response Count: " + outboundConfig.getResponseCount());
        out.println(" • Min OB Count: " + outboundConfig.getMinObCount());
        out.println(" • Min Unique Server IPs: " + outboundConfig.getMinUniqueServerIps());
        out.println(" • Client Country: " + outboundConfig.getClientCountry());
        out.println(" • Server Country: " + outboundConfig.getServerCountry());
        out.println(" • Mail Type: " + outboundConfig.getMail().getType());
        out.println(" • Skip Mail if sent in last " + outboundConfig.getMail().getSkipDaysIfMailed() + " day(s)");
        out.println(" • Mail Enabled: " + outboundConfig.getMail().isEnabled());
    }

    private void executeOutboundCheck() {
        out.println("OUTBOUND");

    }
    public void executeSshCheck() {
        List<Mail> mlist = null ;
        List<SshData> sshList = null;

        // Fetch previously mailed records once
        mlist = mailService.fetchMailRecords("", "", "", "", sshConfig.getMail().getType(), sshConfig.getMail().getSkipDaysIfMailed());
        sshList = sshServices.getSsh(sshConfig.getPort(), sshConfig.getAsn(), sshConfig.getDuration().getHours(),
                sshConfig.getMinFlowCount(), sshConfig.getResponseCount(), sshConfig.getPasswordBasedCondition(), mlist);
        out.println("List Size: " + sshList.size());


        Set<String> mailedIps = new HashSet<>();

        for (SshData flowData : sshList) {
            String srcIp = flowData.getSrcIp();

            // Skip if already handled in this run
            if (mailedIps.contains(srcIp)) {
                out.println("Duplicate IP in current run, skipping: " + srcIp);
                continue;
            }

            // Check DB history
            List<Mail> mailHistory = mailService.fetchMailRecords("", "", srcIp, "",
                    sshConfig.getMail().getType(), sshConfig.getMail().getSkipDaysIfMailed());
            if (Util.alreadyMailed(mailHistory, srcIp)) {
                out.println("Skipping already mailed IP from DB: " + srcIp);
                continue;
            }

            // Prepare mail content
            String subject = Template.getSSHCybSubject(srcIp);
            String body = Template.getSSHCybBody(srcIp, "", sshConfig.getPort());

            // Send mail (retry once on failure)
            boolean sent = sendMail(mailConfig.getTo(), subject, body ,mailConfig.getCc());
            if (!sent) {
                out.println("First attempt failed, retrying for IP: " + srcIp);
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
                out.println(Util.outSuccess("✅ Mail sent to IP: " + srcIp));
            } else {
                out.println("❌ Failed to send mail to IP: " + srcIp + " after 2 attempts");
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
            out.println(Util.outSuccess("send email: " + to));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }

    // Insert a mail record into DB
    public void insertMailRecord(String vmId, String vmName, String vmIp, String vmOwner, String mailType) {
        String sql = Query.insertMail(vmId, vmName, vmIp, vmOwner, mailType);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            out.println(Util.outSuccess("Data inserted"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
