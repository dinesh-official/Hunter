package com.devkng.Hunter.service;



import com.devkng.Hunter.config.ClickHouseConfig;
import com.devkng.Hunter.config.SshConfig;
import com.devkng.Hunter.model.FlowData;
import com.devkng.Hunter.model.Mail;
import com.devkng.Hunter.utility.Query;
import com.devkng.Hunter.utility.Ssh;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SshServices {

    private final SshConfig sshConfig;
    private final HikariDataSource dataSource;
    private final Set<String> noPass = new HashSet<>();

    public SshServices(ClickHouseConfig db, SshConfig sshConfig) {

        // Initialize HikariCP DataSource for ClickHouse DB
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(db.getUrl());
        config.setUsername(db.getUsername());
        config.setPassword(db.getPassword());
        config.setMaximumPoolSize(10);  // Adjust as needed

        this.sshConfig = sshConfig;
        this.dataSource = new HikariDataSource(config);

    }

    public List<FlowData> getSsh(int ipDstPort, int dstAsn, int intervalHour, int flowCountThreshold, int maxResults, int noPasswordFlag, List<Mail> mlist) {
        List<FlowData> results = new ArrayList<>();




        String query = Query.getPassword(ipDstPort, dstAsn, intervalHour, flowCountThreshold);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String ip = rs.getString("dst_ip");
                long count = rs.getLong("flow_count");


                if (mlist != null && !mlist.isEmpty() && mlist.stream().anyMatch(mail -> mail.getVmIp().equals(ip))) {
                    continue;
                }



                if (noPasswordFlag == 2) {
                    results.add(new FlowData(ip, count));
                    if (results.size() >= maxResults) {
                        break;
                    }
                    continue;
                }

                if (noPass.contains(ip)) {
                    if (noPasswordFlag == 1) {
                        results.add(new FlowData(ip, count));
                    }
                    continue;
                }

                if (Ssh.supportsPasswordAuth(ip)) {
                    if (noPasswordFlag == 0) {
                        results.add(new FlowData(ip, count));
                    }
                } else {
                    noPass.add(ip);
                    if (noPasswordFlag == 1) {
                        results.add(new FlowData(ip, count));
                    }
                }



                if (results.size() >= maxResults) {
                    break;
                }



            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
}
