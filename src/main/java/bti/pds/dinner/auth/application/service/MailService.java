package bti.pds.dinner.auth.application.service;


import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.infrastructure.mail.EmailSender;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final EmailSender emailSender;

    public MailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendVerificationEmail(@NonNull User user, String token) {
        String verifyLink = "http://localhost:8080/api/auth/verify?token=" + token;
        String subject = "Please verify your email";
        String content = """
                <h1> Welcome, %s!</h1>
                <p>Please click the link below to verify your account:</p>
                <a href="%s">Verify Account</a>
                """.formatted(user.getName(), verifyLink);

        emailSender.sendEmail(user.getEmail(), subject, content);
    }

    public void sendResetPasswordEmail(@NonNull User user, String token) {
        String resetLink = "http://localhost:8080/api/auth/password-reset/validate?token=" + token;
        String subject = "Reset your password";
        String content = """
                <h1> Hello, %s!</h1>
                <p>Please click the link below to reset your password:</p>
                <a href="%s">Reset Password</a>
                """.formatted(user.getName(), resetLink);

        emailSender.sendEmail(user.getEmail(), subject, content);
    }
}
