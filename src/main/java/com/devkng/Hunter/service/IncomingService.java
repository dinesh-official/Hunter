package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.model.IncomingData;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.Util;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class IncomingService {

    private final HikariDataSource dataSource;

    public IncomingService(ClickHouseConfig db) {
        // Initialize HikariDataSource using config
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);
    }

    public List<IncomingData> getIncomingData(int dstAsn, List<Integer> dstPorts, int minRequestCount, int intervalHours, int limit
    ) {
        List<IncomingData> results = new ArrayList<>();



        String sql = Query.getIncomingTrafficQuery(dstAsn, dstPorts,minRequestCount, intervalHours, limit);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                IncomingData data = new IncomingData();
                data.setIp(rs.getString("ip"));
                data.setRequestCount(rs.getInt("incoming_request_count"));
                data.setUniqueSourceIps(rs.getInt("unique_source_ips"));
                List<Integer> openPorts = checkOpenPorts(data.getIp(), dstPorts, 2000);
                data.setOpenPorts(openPorts);
                results.add(data);


            }
        } catch (SQLException e) {
            e.printStackTrace(); // Better: use logger to log errors
        }

        return results;
    }



    public List<Integer> checkOpenPorts(String ip, List<Integer> ports, int timeoutMs) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(ports.size(), 20)); // limit max threads

        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        for (int port : ports) {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(ip, port), timeoutMs);
                    return port;  // port is open
                } catch (Exception e) {
                    return null;  // port closed or unreachable
                }
            }, executor);
            futures.add(future);
        }

        List<Integer> openPorts = futures.stream()
                .map(CompletableFuture::join)    // wait for all to complete
                .filter(port -> port != null)    // filter only open ports
                .collect(Collectors.toList());

        executor.shutdown();

        return openPorts;
    }



}
