package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.config.SshConfig;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.model.SshData;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.SshPasswordChecker;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

@Service
public class SshServices {

    private final SshConfig sshConfig;
    private final HikariDataSource dataSource;
    private final Set<String> noPass = new HashSet<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(200); // Configurable pool size

    public SshServices(ClickHouseConfig db, SshConfig sshConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);

        this.sshConfig = sshConfig;
        this.dataSource = new HikariDataSource(config);
    }

    public List<SshData> getSsh(int ipDstPort, int dstAsn, int intervalHour, int flowCountThreshold,
                                int maxResults, int noPasswordFlag, List<Mail> mlist) {

        List<SshData> results = Collections.synchronizedList(new ArrayList<>());
        Map<String, Long> ipToCount = new HashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        String query = Query.getPassword(ipDstPort, dstAsn, intervalHour, flowCountThreshold);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String ip = rs.getString("dst_ip");
                long count = rs.getLong("flow_count");

                // Skip matched VMs
                if (mlist != null && mlist.stream().anyMatch(mail -> mail.getVmIp().equals(ip))) {
                    continue;
                }

                ipToCount.put(ip, count);

                if (noPasswordFlag == 2) {
                    results.add(new SshData(ip, count));
                    if (results.size() >= maxResults) break;
                } else if (noPass.contains(ip)) {
                    if (noPasswordFlag == 1) {
                        results.add(new SshData(ip, count));
                    }
                } else {
                    futures.add(executor.submit(() -> {
                        try {
                            Future<Boolean> future = SshPasswordChecker.check(ip);
                            boolean result = future.get(7, TimeUnit.SECONDS); // Optional timeout
                            if (result && noPasswordFlag == 0) {
                                results.add(new SshData(ip, ipToCount.get(ip)));
                            } else if (!result) {
                                noPass.add(ip);
                                if (noPasswordFlag == 1) {
                                    results.add(new SshData(ip, ipToCount.get(ip)));
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("SSH check failed for " + ip + ": " + e.getMessage());
                        }
                    }));
                }

                if (results.size() >= maxResults) break;
            }

            // Wait for all SSH check futures to complete
            for (Future<?> f : futures) {
                try {
                    f.get(); // wait for each future to complete
                } catch (Exception ignored) {}
                if (results.size() >= maxResults) break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results.size() > maxResults
                ? results.subList(0, maxResults)
                : results;
    }
}
