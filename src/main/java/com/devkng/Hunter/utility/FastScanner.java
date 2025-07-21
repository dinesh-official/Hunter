package com.devkng.Hunter.utility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FastScanner {

    private static final int TIMEOUT_MS = 200;         // Socket timeout
    private static final int MAX_RETRIES = 2;          // Retries per port
    private static final int GLOBAL_TIMEOUT_SEC = 5;   // Timeout for total scan
    private static final int MAX_THREADS = 500;        // Bounded thread pool to prevent OOM
    private static final int TASK_QUEUE_SIZE = 10000;  // Backpressure to avoid flooding memory

    public static Map<String, List<Integer>> scan(List<String> ips, List<Integer> ports, int limit) {
        Map<String, List<Integer>> result = new ConcurrentHashMap<>();
        Map<Integer, Set<String>> portToIps = new ConcurrentHashMap<>();
        for (int port : ports) {
            portToIps.put(port, ConcurrentHashMap.newKeySet());
        }

        AtomicBoolean done = new AtomicBoolean(false);
        int retryCount = 0;
        boolean scanCompleted = false;

        // OOM-safe thread pool with queue
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                MAX_THREADS,
                MAX_THREADS,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(TASK_QUEUE_SIZE),  // Bounded queue
                new ThreadPoolExecutor.CallerRunsPolicy()    // Backpressure: runs in caller thread if full
        );

        while (!scanCompleted && retryCount < 2) {
            List<Future<?>> futures = new ArrayList<>();
            long deadline = System.currentTimeMillis() + GLOBAL_TIMEOUT_SEC * 1000L;

            try {
                for (String ip : ips) {
                    if (done.get()) break;

                    futures.add(executor.submit(() -> {
                        List<Integer> openPorts = new ArrayList<>();

                        for (int port : ports) {
                            if (portToIps.get(port).size() >= limit || done.get()) continue;

                            boolean isOpen = false;
                            for (int i = 0; i < MAX_RETRIES; i++) {
                                try (Socket socket = new Socket()) {
                                    socket.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
                                    isOpen = true;
                                    break;
                                } catch (IOException ignored) {}
                            }

                            if (isOpen) {
                                Set<String> ipSet = portToIps.get(port);
                                synchronized (ipSet) {
                                    if (ipSet.size() < limit && ipSet.add(ip)) {
                                        openPorts.add(port);
                                    }
                                }
                            }

                            if (System.currentTimeMillis() > deadline) break;
                        }

                        if (!openPorts.isEmpty()) {
                            result.put(ip, openPorts);
                        }

                        if (ports.stream().allMatch(p -> portToIps.get(p).size() >= limit)) {
                            done.set(true);
                        }
                    }));
                }

                for (Future<?> future : futures) {
                    try {
                        future.get(GLOBAL_TIMEOUT_SEC, TimeUnit.SECONDS);
                    } catch (TimeoutException ignored) {
                        done.set(true);
                    }
                }

                scanCompleted = true;

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            } catch (RejectedExecutionException e) {
                System.err.println("Task rejected due to memory limits: " + e.getMessage());
                break;
            }

            retryCount++;
            if (!scanCompleted) {
                System.err.println("Scan timed out, retrying attempt: " + retryCount);
            }
        }

        executor.shutdownNow();
        return result;
    }

    public static Map<String, List<Integer>> scan2(List<String> ips, List<Integer> ports, int limit) {
        Map<String, List<Integer>> result = new ConcurrentHashMap<>();
        Map<Integer, Set<String>> portToIps = new ConcurrentHashMap<>();
        for (int port : ports) {
            portToIps.put(port, ConcurrentHashMap.newKeySet());
        }

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<?>> futures = new ArrayList<>();

        for (String ip : ips) {
            futures.add(executor.submit(() -> {
                List<Integer> openPorts = new ArrayList<>();

                for (int port : ports) {
                    Set<String> ipSet = portToIps.get(port);
                    if (ipSet.size() >= limit) continue;

                    boolean isOpen = false;
                    for (int i = 0; i < MAX_RETRIES; i++) {
                        try (Socket socket = new Socket()) {
                            socket.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
                            isOpen = true;
                            break;
                        } catch (IOException ignored) {}
                    }

                    if (isOpen) {
                        synchronized (ipSet) {
                            if (ipSet.size() < limit && ipSet.add(ip)) {
                                openPorts.add(port);
                            }
                        }
                    }
                }

                if (!openPorts.isEmpty()) {
                    result.put(ip, openPorts);
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get(GLOBAL_TIMEOUT_SEC, TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
            }
        }

        executor.shutdownNow();
        return result;
    }

    public static Map<String, List<Integer>> ultraReliableScan(List<String> ips, List<Integer> ports, int limit) {
        Map<String, List<Integer>> result = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<?>> futures = new ArrayList<>();

        for (String ip : ips) {
            futures.add(executor.submit(() -> {
                List<Integer> openPorts = new ArrayList<>();
                for (int port : ports) {
                    boolean isOpen = false;

                    // RETRY until success or MAX_RETRIES
                    for (int i = 0; i < MAX_RETRIES; i++) {
                        try (Socket socket = new Socket()) {
                            socket.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
                            isOpen = true;
                            break;
                        } catch (IOException ignored) {
                            // Retry
                        }
                    }

                    // Save if open
                    if (isOpen) openPorts.add(port);
                }

                if (!openPorts.isEmpty()) {
                    result.put(ip, openPorts); // don't miss any
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS); // Give longer time per IP
            } catch (Exception e) {
                future.cancel(true); // Allow cancellation, but don’t silently skip
            }
        }

        executor.shutdownNow();
        return result;
    }

}
