package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.config.OpenPortsConfig;
import com.devkng.Hunter.model.MailData;
import com.devkng.Hunter.model.OpenPortsData;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.FastScanner;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class OpenPortsService {

    private final HikariDataSource dataSource;
    private final MailService mailService;
    private final OpenPortsConfig openPortsConfig;

    private final ExecutorService executor;

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
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public List<OpenPortsData> getIncomingData(int dstAsn, List<Integer> dstPorts, int minRequestCount,
                                               int intervalHours, int limit, boolean isScheduler) {
        Map<String, OpenPortsData> dataMap = new ConcurrentHashMap<>();
        List<String> ipList = Collections.synchronizedList(new ArrayList<>());

        String sql = Query.getIncomingTrafficQuery(dstAsn, dstPorts, minRequestCount, intervalHours, limit * 100);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Callable<Void>> tasks = new ArrayList<>();

            while (rs.next()) {
                String ip = rs.getString("ip");
                int reqCount = rs.getInt("incoming_request_count");
                int uniqSrcIps = rs.getInt("unique_source_ips");

                tasks.add(() -> {
                    OpenPortsData data = new OpenPortsData();
                    data.setIp(ip);
                    data.setRequestCount(reqCount);
                    data.setUniqueSourceIps(uniqSrcIps);
                    dataMap.put(ip, data);
                    ipList.add(ip);
                    return null;
                });
            }

            executor.invokeAll(tasks);

            // Parallel port scan
            Map<String, List<Integer>> openPortsMap = FastScanner.ultraReliableScan(ipList, dstPorts, limit);

            List<CompletableFuture<OpenPortsData>> futureResults = new ArrayList<>();

            for (Map.Entry<String, List<Integer>> entry : openPortsMap.entrySet()) {
                futureResults.add(CompletableFuture.supplyAsync(() -> {
                    String ip = entry.getKey();
                    List<Integer> openPorts = entry.getValue();
                    if (openPorts == null || openPorts.isEmpty()) return null;

                    OpenPortsData data = dataMap.get(ip);
                    if (data == null) return null;

                    if (!isScheduler) {
                        data.setOpenPorts(openPorts);
                        return data;
                    } else {
                        List<Integer> unmailedPorts = openPorts.parallelStream()
                                .filter(port -> {
                                    List<MailData> mails = mailService.fetchMailRecords(
                                            "", "", ip, "",
                                            openPortsConfig.getMail().getType() + "-" + port,
                                            openPortsConfig.getMail().getSkipDaysIfMailed()
                                    );
                                    return mails.isEmpty();
                                })
                                .collect(Collectors.toList());

                        if (!unmailedPorts.isEmpty()) {
                            data.setOpenPorts(unmailedPorts);
                            return data;
                        }
                    }
                    return null;
                }, executor));
            }

            List<OpenPortsData> results = futureResults.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(OpenPortsData::getRequestCount).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            return results;

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
