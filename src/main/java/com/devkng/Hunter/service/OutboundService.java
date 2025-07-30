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

    private static final Logger logger = LoggerFactory.getLogger(OutboundService.class);

    private final HikariDataSource dataSource;
    private final OutboundConfig outboundConfig;

    /**
     * Constructor to initialize ClickHouse database connection pool
     * and outbound configuration using HikariCP.
     */
    public OutboundService(ClickHouseConfig db, OutboundConfig outboundConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10); // Tune this based on load
        this.dataSource = new HikariDataSource(config);
        this.outboundConfig = outboundConfig;
    }

    /**
     * Fetch outbound connection statistics from ClickHouse.
     *
     * @param ipDstPort            Port to filter by (0 means all)
     * @param dstAsn               Destination ASN
     * @param srcAsn               Source ASN
     * @param intervalHour         Time window to check (e.g., last 24h)
     * @param clientCountry        Not used (reserved for future)
     * @param serverCountry        Not used (reserved for future)
     * @param responseCount        Max number of records to return
     * @param minObCount           Minimum OB connection count threshold
     * @param minUniqueServerIps   Minimum number of unique server IPs
     * @param mlist                Already mailed list to avoid duplicates
     * @return                     Filtered list of outbound stats
     */
    public List<OutBoundData> getOutboundData(
            int ipDstPort,
            int dstAsn,
            int srcAsn,
            int intervalHour,
            String clientCountry,
            String serverCountry,
            int responseCount,
            int minObCount,
            int minUniqueServerIps,
            List<MailData> mlist
    ) {
        List<OutBoundData> results = new ArrayList<>();

        // Build dynamic ClickHouse query
        String query = Query.getObQuery(ipDstPort, srcAsn, dstAsn, intervalHour);

        // Cache previously mailed IP + type-port combinations
        Set<String> alreadyMailedCache = mlist != null
                ? mlist.stream()
                .map(m -> m.getVmIp() + "->" + m.getMailType())
                .collect(Collectors.toSet())
                : Collections.emptySet();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next() && results.size() < responseCount) {
                OutBoundData stat = new OutBoundData();
                stat.setClientIp(rs.getString("client_ip"));
                stat.setObCount(rs.getInt("OB_Count"));
                stat.setUniqueServerIps(rs.getInt("unique_server_ips"));

                // Convert port string (e.g., "[80, 443]") to List<Integer>
                String portArray = rs.getString("destination_ports");
                List<Integer> ports = Arrays.stream(
                                portArray.replaceAll("[\\[\\]\\s]", "").split(","))
                        .filter(p -> !p.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                stat.setDestinationPorts(ports);

                // Skip if already mailed for any port
                boolean alreadyMailed = ports.stream()
                        .filter(p -> p != 0)
                        .map(p -> stat.getClientIp() + "->" + outboundConfig.getMail().getType() + "-" + p)
                        .anyMatch(alreadyMailedCache::contains);

                if (alreadyMailed) continue;

                // Apply outbound count and unique IP filters
                if (stat.getObCount() >= minObCount && stat.getUniqueServerIps() >= minUniqueServerIps) {
                    results.add(stat);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch outbound data", e);
            throw new RuntimeException("Database query failed", e);
        }

        return results;
    }
}
