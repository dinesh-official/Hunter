package com.devkng.Hunter.scheduler;


import com.devkng.Hunter.config.MailConfig;
import com.devkng.Hunter.config.SshConfig;
import com.devkng.Hunter.model.FlowData;
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
    private boolean hasLoggedOnce = false;
    // ANSI color codes




    public MyScheduler(JavaMailSender mailSender, SshServices sshServices, MailService mailService, DataSource dataSource, SshConfig sshConfig, MailConfig mailConfig) {
        this.mailSender = mailSender;
        this.sshServices = sshServices;
        this.mailService = mailService;
        this.dataSource = dataSource;
        this.sshConfig = sshConfig;
        this.mailConfig = mailConfig;

    }

    @Scheduled(cron = "${schedule.ssh}")
    public void runDailyTask() {
        if (!hasLoggedOnce) {

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

            if (sshConfig.getMail().isEnabled()) {
                out.println("SSH Mail Alert is ENABLED. Executing SSH flow check to send mail...");
            } else {
                out.println("SSH Mail Alert skipped due to configuration.");
            }
            hasLoggedOnce = true; // ensure this message logs only once
        }

        if (sshConfig.getMail().isEnabled()) {
            executeSshCheck();
        }

    }

    public void executeSshCheck() {
        List<Mail> mlist = null ;
        List<FlowData> sshList = null;

        // Fetch previously mailed records once
        mlist = mailService.fetchMailRecords("", "", "", "", sshConfig.getMail().getType(), sshConfig.getMail().getSkipDaysIfMailed());
        sshList = sshServices.getSsh(sshConfig.getPort(), sshConfig.getAsn(), sshConfig.getDuration().getHours(),
                sshConfig.getMinFlowCount(), sshConfig.getResponseCount(), sshConfig.getPasswordBasedCondition(), mlist);
        out.println("List Size: " + sshList.size());


        Set<String> mailedIps = new HashSet<>(); // Avoid duplicate sends within same run

        for (FlowData flowData : sshList) {
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
