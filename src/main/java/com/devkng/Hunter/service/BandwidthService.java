package com.devkng.Hunter.service;

import com.devkng.Hunter.config.BandwidthConfig;
import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.model.BandwidthData;
import com.devkng.Hunter.model.MailData;
import com.devkng.Hunter.utility.Query;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BandwidthService {

    private final HikariDataSource dataSource;
    private final BandwidthConfig bandwidthConfig;
    private final MailService mailService;
    private Set<String> alreadyMailedCache = new HashSet<>();

    public BandwidthService(ClickHouseConfig db, HikariDataSource unusedDataSource, BandwidthConfig bandwidthConfig, MailService mailService) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);
        this.bandwidthConfig = bandwidthConfig;
        this.mailService = mailService;
    }

    public List<BandwidthData> getBandwidthData(int intervalHours, int dstAsn, int limit, int minGb, boolean isScheduler) {
        List<BandwidthData> result = new ArrayList<>();

        if (isScheduler) {
            List<MailData> mailList = mailService.fetchMailRecords(
                    "", "", "", "", bandwidthConfig.getMail().getType(), bandwidthConfig.getMail().getSkipDaysIfMailed()
            );

            alreadyMailedCache = (mailList != null)
                    ? mailList.stream()
                    .map(m -> m.getVmIp() + "->" + m.getMailType())
                    .collect(Collectors.toSet())
                    : new HashSet<>();
        }
        long minBytes = minGb * 1024L * 1024L * 1024L;
        String sql = Query.getBandwidthQuery(dstAsn, intervalHours,minBytes);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next() && result.size() < limit) {
                double totalGb = rs.getDouble("total_gb");
                if (totalGb < minGb) continue;

                String srcIp = rs.getString("src_ip");
                String mailKey = srcIp + "->" + bandwidthConfig.getMail().getType();

                // Skip if already mailed (only in scheduler mode)
                if (isScheduler && alreadyMailedCache.contains(mailKey)) continue;

                BandwidthData b = new BandwidthData();
                b.setSrcIp(srcIp);
                b.setDstAsn(rs.getInt("SRC_ASN"));
                b.setTotalGb(totalGb);
                if (!(limit==result.size())){
                result.add(b);
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bandwidth data", e);
        }

        return result;
    }
}
