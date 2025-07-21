package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.config.OpenPortsConfig;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.model.OpenPortsData;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.FastScanner;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenPortsService {

    private final HikariDataSource dataSource;
    private final MailService mailService;
    private final OpenPortsConfig openPortsConfig;

    public OpenPortsService(ClickHouseConfig db, MailService mailService, OpenPortsConfig openPortsConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setConnectionTimeout(4000);
        config.setIdleTimeout(300000);
        config.setMaximumPoolSize(16);

        this.dataSource = new HikariDataSource(config);
        this.mailService = mailService;
        this.openPortsConfig = openPortsConfig;
    }

    public List<OpenPortsData> getIncomingData(int dstAsn, List<Integer> dstPorts, int minRequestCount,
                                               int intervalHours, int limit, boolean isScheduler) {
        List<OpenPortsData> allData = new ArrayList<>();
        Map<String, OpenPortsData> dataMap = new HashMap<>();

        String sql = Query.getIncomingTrafficQuery(dstAsn, dstPorts, minRequestCount, intervalHours, limit * 100);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                OpenPortsData data = new OpenPortsData();
                String ip = rs.getString("ip");
                data.setIp(ip);
                data.setRequestCount(rs.getInt("incoming_request_count"));
                data.setUniqueSourceIps(rs.getInt("unique_source_ips"));

                allData.add(data);
                dataMap.put(ip, data);
            }

            // Batch scan all IPs for open ports with per-port limit
            List<String> allIps = allData.stream().map(OpenPortsData::getIp).collect(Collectors.toList());
            Map<String, List<Integer>> openPortsMap = FastScanner.ultraReliableScan(allIps, dstPorts, limit);

            List<OpenPortsData> filteredResults = new ArrayList<>();

            for (String ip : openPortsMap.keySet()) {
                List<Integer> openPorts = openPortsMap.get(ip);
                if (openPorts == null || openPorts.isEmpty()) continue;

                OpenPortsData data = dataMap.get(ip);

                if (!isScheduler) {
                    data.setOpenPorts(openPorts);
                    filteredResults.add(data);
                } else {
                    List<Integer> unmailedPorts = new ArrayList<>();
                    for (int port : openPorts) {
                        List<Mail> mails = mailService.fetchMailRecords(
                                "", "", ip, "",
                                openPortsConfig.getMail().getType() + "-" + port,
                                openPortsConfig.getMail().getSkipDaysIfMailed()
                        );
                        if (mails.isEmpty()) {
                            unmailedPorts.add(port);
                        }
                    }
                    if (!unmailedPorts.isEmpty()) {
                        data.setOpenPorts(unmailedPorts);
                        filteredResults.add(data);
                    }
                }
            }

           // return filteredResults.stream().limit(limit).collect(Collectors.toList());

            // sort final results by requestCount descending
            return filteredResults.stream()
                    .sorted(Comparator.comparingInt(OpenPortsData::getRequestCount).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
