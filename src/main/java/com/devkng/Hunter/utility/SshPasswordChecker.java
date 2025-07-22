package com.devkng.Hunter.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SshPasswordChecker {

    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int THREAD_POOL_SIZE = Math.min(200, CORE_COUNT * 2); // safer value
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    public static Future<Boolean> check(String ip) {
        return executor.submit(() -> {
            try {
                // Add a small delay (optional)
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}

            if (cache.containsKey(ip)) return cache.get(ip);
            boolean result = checkInternal(ip, false);
            cache.put(ip, result);
            return result;
        });
    }

    private static boolean checkInternal(String ip, boolean retrying) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ssh",
                    "-o", "BatchMode=yes",
                    "-o", "ConnectTimeout=5",
                    "-o", "StrictHostKeyChecking=no",
                    "root@" + ip,
                    "exit"
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line.toLowerCase()).append("\n");
            }

            boolean finished = process.waitFor(6, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("[" + ip + "] SSH check timed out.");
                return false;
            }

            String sshOutput = output.toString();
            int exit = process.exitValue();

            System.out.println("[" + ip + "] SSH output:\n" + sshOutput.trim());

            if ((sshOutput.contains("offending key") || sshOutput.contains("host key verification failed")) && !retrying) {
                clearKnownHost(ip);
                return checkInternal(ip, true); // Retry once
            }

            if (sshOutput.contains("permission denied (password") ||
                    sshOutput.contains("keyboard-interactive")) {
                return true; // Password login supported
            }

            if (sshOutput.contains("permission denied (publickey") ||
                    sshOutput.contains("no supported authentication methods available")) {
                return false; // Public key only
            }

            if (exit == 0) {
                return false; // Already authenticated (key-based)
            }

            if (sshOutput.contains("connection timed out") ||
                    sshOutput.contains("connection refused")) {
                return false; // Host unreachable
            }

        } catch (Exception e) {
            System.err.println("[" + ip + "] SSH check failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private static void clearKnownHost(String ip) {
        try {
            ProcessBuilder cleaner = new ProcessBuilder(
                    "ssh-keygen",
                    "-f",
                    System.getProperty("user.home") + "/.ssh/known_hosts",
                    "-R", ip
            );
            cleaner.redirectErrorStream(true);
            Process process = cleaner.start();
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
            System.out.println("[" + ip + "] known_hosts entry cleared.");
        } catch (Exception e) {
            System.err.println("Failed to clear known_hosts for " + ip + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
