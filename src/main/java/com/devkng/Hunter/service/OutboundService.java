package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.config.OutboundConfig;
import com.devkng.Hunter.model.OutBoundData;
import com.devkng.Hunter.utility.Query;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class OutboundService {

    private final HikariDataSource dataSource;
    private final OutboundConfig outboundConfig;

    public OutboundService(ClickHouseConfig db, OutboundConfig outboundConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);  // Adjust as needed
        this.dataSource = new HikariDataSource(config);
        this.outboundConfig = outboundConfig;
    }

    public List<OutBoundData> getOutboundData(int ipDstPort, int dstAsn, int srcAsn, int intervalHour,
                                              String clientCountry, String serverCountry, int responseCount,int minObCount ,int minUniqueServerIps) {
        List<OutBoundData> results = new ArrayList<>();

        String query = Query.getObQuery(ipDstPort, srcAsn, dstAsn, intervalHour, responseCount);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                OutBoundData stat = new OutBoundData();
                stat.setClientIp(rs.getString("client_ip"));
                stat.setObCount(rs.getInt("OB_Count"));
                stat.setUniqueServerIps(rs.getInt("unique_server_ips"));

                String portArray = rs.getString("destination_ports");
                List<Integer> ports = Arrays.stream(
                                portArray.replaceAll("[\\[\\]\\s]", "").split(","))
                        .filter(p -> !p.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                stat.setDestinationPorts(ports);

                results.add(stat);
            }
            if (minObCount > 0) {
                results = results.stream()
                        .filter(d -> d.getObCount() >= minObCount)
                        .collect(Collectors.toList());
            }
            if (minUniqueServerIps > 0) {
                results = results.stream()
                        .filter(d -> d.getUniqueServerIps() >= minUniqueServerIps)
                        .collect(Collectors.toList());
            }


        } catch (SQLException e) {
            e.printStackTrace();  // Optional: use a logger in real code
            throw new RuntimeException("Failed to fetch OB data", e);
        }

        return results;
    }
}
