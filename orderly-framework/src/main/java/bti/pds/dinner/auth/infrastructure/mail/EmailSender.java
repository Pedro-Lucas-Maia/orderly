package bti.pds.dinner.auth.infrastructure.mail;

public interface EmailSender {
    void sendEmail(String to, String subject, String content);
}
