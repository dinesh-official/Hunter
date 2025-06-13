package com.devkng.Hunter.service;


import com.devkng.Hunter.config.MySqlConfig;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.utility.Query;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class MailService {

    private final DataSource dataSource;  // use connection pool instead of raw DriverManager

    public MailService(MySqlConfig db, JavaMailSender mailSender) throws SQLException {
        // Setup a pooled DataSource using HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10); // adjust as needed

        this.dataSource = new HikariDataSource(config);
    }

    // Fetch mail records based on filters and date interval
    public List<Mail> fetchMailRecords(String vmId, String vmOwner, String vmIp, String vmName, String mailType, int lastDays) {
        long start = System.currentTimeMillis();
        List<Mail> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Query.getMail(lastDays, vmId, vmOwner, vmIp, vmName, mailType))) {

            while (rs.next()) {
                Mail record = new Mail();
                record.setId(rs.getLong("id"));
                record.setCreatedAd(rs.getTimestamp("createdAd").toLocalDateTime());
                record.setVmId(rs.getString("vmId"));
                record.setVmName(rs.getString("vmName"));
                record.setVmIp(rs.getString("vmIp"));
                record.setVmOwner(rs.getString("vmOwner"));
                record.setMailType(rs.getString("mailType"));
                results.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return results;

    }


}
