package com.devkng.Hunter.service;

import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.model.BandwidthData;
import com.devkng.Hunter.utility.Query;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class BandwidthService {

    private final HikariDataSource dataSource;

    public BandwidthService(ClickHouseConfig db, HikariDataSource dataSource) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);  // Adjust as needed
        this.dataSource = new HikariDataSource(config);
    }

    public List<BandwidthData> getBandwidthData(int intervalHours, int dstAsn, long mBThreshold, int limit) {

        List<BandwidthData> result = new ArrayList<>();
        String sql = Query.getBandwidthQuery(dstAsn, intervalHours, mBThreshold, limit);


        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BandwidthData b = new BandwidthData();
                b.setSrcIp(rs.getString("src_ip"));
                b.setDstAsn(rs.getInt("DST_ASN"));
                //b.setTotalBytes(rs.getLong("total_bytes"));
                b.setTotalGb(rs.getDouble("total_gb"));
                result.add(b);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bandwidth data", e);
        }
        return result;
    }
}
