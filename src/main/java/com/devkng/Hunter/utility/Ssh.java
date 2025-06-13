package com.devkng.Hunter.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Ssh {

    public static boolean supportsPasswordAuth(String ip) {
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

            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }

            int exitCode = process.exitValue();
            String outStr = output.toString();

            if (outStr.contains("permission denied (password") || outStr.contains("password")) {
                return true;
            } else if (outStr.contains("permission denied (publickey") || outStr.contains("publickey")) {
                return false;
            } else if (exitCode == 0) {
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error checking SSH for " + ip + ": " + e.getMessage());
        }

        return false;
    }

}
