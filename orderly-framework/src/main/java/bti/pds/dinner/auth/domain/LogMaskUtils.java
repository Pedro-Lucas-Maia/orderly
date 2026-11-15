package bti.pds.dinner.auth.domain;

import org.jspecify.annotations.NonNull;

public class LogMaskUtils {
    public static @NonNull String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) return "***";
        return "***." + cpf.substring(cpf.length() - 4);
    }

    public static @NonNull String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int atIndex = email.indexOf("@");
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
