package com.devkng.Hunter.utility;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;

public class DynamicHikariCPManager {

    private static final String DB_HOST = "localhost"; // Replace with your DB host
    private static final int DB_PORT = 3306; // Replace with your DB port

    private static HikariDataSource dataSource;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static HikariDataSource initDynamicDataSource(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);


        dataSource = new HikariDataSource(config);

        // Schedule dynamic tuning every 30 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                adjustPoolAtRuntime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.SECONDS);

        return dataSource;
    }


    private static void adjustPoolAtRuntime() {
        if (dataSource == null) return;

        int cpuCores = Runtime.getRuntime().availableProcessors();
        int newMaxPoolSize = Math.min(cpuCores * 2, 50);

        long dbLatency = measureDbLatency(DB_HOST, DB_PORT);
        long newTimeout = Math.min(10_000, dbLatency * 3 + 1000);

        int currentActive = dataSource.getHikariPoolMXBean().getActiveConnections();
        int total = dataSource.getHikariPoolMXBean().getTotalConnections();

        int idleTimeout = (currentActive < total / 2) ? 60_000 : 300_000;

        // Apply dynamically
        dataSource.setMaximumPoolSize(newMaxPoolSize);
        dataSource.setConnectionTimeout(newTimeout);
        dataSource.setIdleTimeout(idleTimeout);

        System.out.println("[DynamicHikariCP] Updated settings:");
        System.out.println("- Max Pool Size: " + newMaxPoolSize);
        System.out.println("- Timeout: " + newTimeout + "ms");
        System.out.println("- Idle Timeout: " + idleTimeout + "ms");
    }

    private static long measureDbLatency(String host, int port) {
        try (Socket socket = new Socket()) {
            long start = System.nanoTime();
            socket.connect(new InetSocketAddress(host, port), 1000);
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        } catch (Exception e) {
            return 1000; // Fallback latency
        }
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
        scheduler.shutdownNow();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
