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
import java.util.stream.Stream;

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
        String sql = Query.getIncomingTrafficQuery(dstAsn, dstPorts, minRequestCount, intervalHours, limit * 100);

        Map<String, OpenPortsData> dataMap = new HashMap<>();
        List<String> allIps = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String ip = rs.getString("ip");
                OpenPortsData data = new OpenPortsData();
                data.setIp(ip);
                data.setRequestCount(rs.getInt("incoming_request_count"));
                data.setUniqueSourceIps(rs.getInt("unique_source_ips"));
                dataMap.put(ip, data);
                allIps.add(ip);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        if (allIps.isEmpty()) return Collections.emptyList();

        // 🚀 Scan IPs in parallel for open ports
        Map<String, List<Integer>> openPortsMap = FastScanner.ultraReliableScan(allIps, dstPorts, limit);

        // ✅ Filter results (parallel if isScheduler)
        Stream<Map.Entry<String, List<Integer>>> stream = openPortsMap.entrySet().stream();
        if (isScheduler) stream = stream.parallel();

        List<OpenPortsData> filteredResults = stream.map(entry -> {
                    String ip = entry.getKey();
                    List<Integer> openPorts = entry.getValue();
                    if (openPorts == null || openPorts.isEmpty()) return null;

                    OpenPortsData data = dataMap.get(ip);
                    if (data == null) return null;

                    if (!isScheduler) {
                        data.setOpenPorts(openPorts);
                        return data;
                    } else {
                        // 🧠 Check mail records only for unmailed ports
                        List<Integer> unmailedPorts = openPorts.stream()
                                .filter(port -> mailService.fetchMailRecords(
                                        "", "", ip, "",
                                        openPortsConfig.getMail().getType() + "-" + port,
                                        openPortsConfig.getMail().getSkipDaysIfMailed()
                                ).isEmpty())
                                .collect(Collectors.toList());

                        if (!unmailedPorts.isEmpty()) {
                            data.setOpenPorts(unmailedPorts);
                            return data;
                        } else {
                            return null;
                        }
                    }
                }).filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(OpenPortsData::getRequestCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        return filteredResults;
    }
}

