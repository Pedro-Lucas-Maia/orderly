package bti.pds.dinner.auth.infrastructure.mail;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResendEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);
    private final String apiKey;

    public ResendEmailSender(@Value("${resend.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void sendEmail(String to, String subject, String content) {

        Resend resend = new Resend(apiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Acme <onboarding@resend.dev>")
                .to(to)
                .subject(subject)
                .html(content)
                .build();
        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("Resend email sent to: {}, with {}", to, data);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}
