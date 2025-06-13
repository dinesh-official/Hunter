package com.devkng.Hunter.onstart;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check and create database if not exists
        jdbc.execute("CREATE DATABASE IF NOT EXISTS hunter");

        // Switch to hunter database
        jdbc.execute("USE hunter");

        // Check if table exists and create it if not
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS mail (
                id INT NOT NULL AUTO_INCREMENT,
                createdAd DATETIME NOT NULL,
                vmId VARCHAR(100),
                vmName VARCHAR(100),
                vmIp VARCHAR(45),
                vmOwner VARCHAR(100),
                mailType VARCHAR(50),
                PRIMARY KEY (id)
            )
        """);

        System.out.println("Hunter DB and mail table verified/created.");
    }
}
