package com.devkng.Hunter.utility;

import com.devkng.Hunter.model.Mail;

import java.util.List;
import java.util.Objects;

public class Util {
    public static final String GREEN = "\u001B[32m";
    public static final String RESET = "\u001B[0m";
    public static boolean alreadyMailed(List<Mail> results, String vmIp) {
        return results.parallelStream()
                .anyMatch(mail ->
                        //Objects.equals(vmId, mail.getVmId()) &&
                               // Objects.equals(vmOwner, mail.getVmOwner()) &&
                                Objects.equals(vmIp,mail.getVmIp())
                               // Objects.equals(mailType,mail.getMailType())
                );
    }
    public static String outSucess(String s) {
        return GREEN + s + RESET;
    }
}
