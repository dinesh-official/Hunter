package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.config.OutboundConfig;
import com.devkng.Hunter.model.MailData;
import com.devkng.Hunter.model.OutBoundData;
import com.devkng.Hunter.utility.Query;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OutboundService {

    private static final Logger log = LoggerFactory.getLogger(OutboundService.class);

    private final HikariDataSource dataSource;
    private final OutboundConfig outboundConfig;

    public OutboundService(ClickHouseConfig db, OutboundConfig outboundConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);  // Tuneable
        this.dataSource = new HikariDataSource(config);
        this.outboundConfig = outboundConfig;
    }

    public List<OutBoundData> getOutboundData(int ipDstPort, int dstAsn, int srcAsn, int intervalHour,
                                              String clientCountry, String serverCountry, int responseCount,
                                              int minObCount, int minUniqueServerIps, List<MailData> mlist) {

        List<OutBoundData> results = new ArrayList<>();
        String query = Query.getObQuery(ipDstPort, srcAsn, dstAsn, intervalHour);
        //log.info("Executing OB Query: {}", query);

        Set<String> alreadyMailedCache = mlist.stream()
                .map(m -> m.getVmIp() + "->" + m.getMailType()) // e.g. 192.168.0.1->OB-443
                .collect(Collectors.toSet());

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                OutBoundData data = new OutBoundData();
                data.setClientIp(rs.getString("client_ip"));
                data.setObCount(rs.getInt("OB_Count"));
                data.setUniqueServerIps(rs.getInt("unique_server_ips"));

                String portArray = rs.getString("destination_ports");
                if (portArray == null || portArray.isEmpty()) continue;

                List<Integer> ports = Arrays.stream(portArray.replaceAll("[\\[\\]\\s]", "").split(","))
                        .filter(p -> !p.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                data.setDestinationPorts(ports);

                // Deduplication check
                boolean alreadyMailed = ports.stream()
                        .filter(p -> p != 0)
                        .map(p -> data.getClientIp() + "->" + outboundConfig.getMail().getType() + "-" + p)
                        .anyMatch(alreadyMailedCache::contains);

                if (alreadyMailed) continue;

                // Filter before adding
                if ((minObCount > 0 && data.getObCount() < minObCount) ||
                        (minUniqueServerIps > 0 && data.getUniqueServerIps() < minUniqueServerIps)) {
                    continue;
                }

                results.add(data);

                // Respect limit
                if (results.size() >= responseCount) {
                    break;
                }
            }

        } catch (SQLException e) {
            log.error("Error executing OB query", e);
            throw new RuntimeException("Failed to fetch OB data", e);
        }

        log.info("Total OB entries returned: {}", results.size());
        return results;
    }
}
