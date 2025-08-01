package com.devkng.Hunter.utility;

import com.devkng.Hunter.model.MailData;

import java.util.List;
import java.util.Objects;

public class Util {
    public static final String GREEN = "\u001B[32m";
    public static final String RESET = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";

    public static boolean alreadyMailed(List<MailData> results, String vmIp) {
        return results.parallelStream()
                .anyMatch(mail ->
                        //Objects.equals(vmId, mail.getVmId()) &&
                               // Objects.equals(vmOwner, mail.getVmOwner()) &&
                                Objects.equals(vmIp,mail.getVmIp())
                               // Objects.equals(mailType,mail.getMailType())
                );
    }
    public static String outSuccess(String s) {
        return GREEN + s + RESET;
    }

    public static String outRed(String s) {
        return RED + s + RESET;
    }

    public static String outGreen(String s) {
        return GREEN + s + RESET;
    }

    public static String outYellow(String s) {
        return YELLOW + s + RESET;
    }

    public static String outBlue(String s) {
        return BLUE + s + RESET;
    }

    public static String outCyan(String s) {
        return CYAN + s + RESET;
    }

}
