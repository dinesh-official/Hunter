package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.config.SshConfig;
import com.devkng.Hunter.model.SshData;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.Ssh;
import com.devkng.Hunter.utility.Util;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SshServices {

    private final SshConfig sshConfig;
    private final HikariDataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(SshServices.class);

    public SshServices(ClickHouseConfig db, SshConfig sshConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);

        this.sshConfig = sshConfig;
        this.dataSource = new HikariDataSource(config);
    }

    public List<SshData> getSsh(int ipDstPort, int dstAsn, int intervalHour, int flowCountThreshold, int maxResults, int noPasswordFlag, List<Mail> mlist) {
        String query = Query.getPassword(ipDstPort, dstAsn, intervalHour, flowCountThreshold);

        int cpuCores = Runtime.getRuntime().availableProcessors();
        int threads = Math.min(cpuCores * 50, 512);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        List<SshData> passwordBasedList = new CopyOnWriteArrayList<>();
        List<SshData> keyBasedList = new CopyOnWriteArrayList<>();

        AtomicInteger pwCount = new AtomicInteger(0);
        AtomicInteger keyCount = new AtomicInteger(0);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            List<Future<?>> futures = new ArrayList<>();

            while (rs.next()) {
                String ip = rs.getString("dst_ip");
                long count = rs.getLong("flow_count");

                // Skip if in mlist
                if (mlist != null && !mlist.isEmpty() && mlist.stream().anyMatch(mail -> mail.getVmIp().equals(ip))) {
                    continue;
                }

                // Stop early if we have enough results
                if ((noPasswordFlag == 0 && pwCount.get() >= maxResults) ||
                        (noPasswordFlag == 1 && keyCount.get() >= maxResults) ||
                        (noPasswordFlag == 2 && (pwCount.get() + keyCount.get()) >= maxResults)) {
                    break;
                }

                futures.add(executor.submit(() -> {
                    try {
                        if (noPasswordFlag == 2) {
                            passwordBasedList.add(new SshData(ip, count)); // raw add
                            pwCount.incrementAndGet();
                            return;
                        }

                        boolean supportsPassword = Ssh.supportsPasswordAuth(ip);
                        SshData data = new SshData(ip, count);

                        if (supportsPassword) {
                            if (noPasswordFlag == 0 && pwCount.get() < maxResults) {
                                passwordBasedList.add(data);
                                pwCount.incrementAndGet();
                            }
                        } else {
                            if (noPasswordFlag == 1 && keyCount.get() < maxResults) {
                                keyBasedList.add(data);
                                keyCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error checking SSH for IP: {}", ip, e);
                    }
                }));
            }

            for (Future<?> future : futures) {
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    future.cancel(true);
                } catch (Exception ignored) {
                }
            }

        } catch (SQLException e) {
            log.error("SQL Exception", e);
        } finally {
            executor.shutdownNow();
        }

        List<SshData> result;

        switch (noPasswordFlag) {
            case 0 -> result = passwordBasedList;
            case 1 -> result = keyBasedList;
            case 2 -> {
                List<SshData> combined = new ArrayList<>();
                combined.addAll(passwordBasedList);
                combined.addAll(keyBasedList);
                result = combined;
            }
            default -> result = Collections.emptyList();
        }

        log.info(Util.outGreen("SSH List Size of API request: " + result.size()));
        return result.stream().limit(maxResults).toList();
    }
}