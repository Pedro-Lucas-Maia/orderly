package bti.pds.dinner.auth.infrastructure.mail;

import bti.pds.dinner.auth.domain.exception.EmailDeliveryException;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResendEmailSender implements EmailSender {
    private final Resend resend;

    public ResendEmailSender(@Value("${resend.api.key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Override
    @Observed(name = "infrastructure.mail.resend")
    public void sendEmail(String to, String subject, String content) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Acme <onboarding@resend.dev>")
                .to(to)
                .subject(subject)
                .html(content)
                .build();
        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.debug("Email dispatched to external provider. provider=Resend, externalMessageId={}", data.getId());
        } catch (Exception e) {
            throw new EmailDeliveryException("Failed to communicate with resend API", e);
        }
    }
}
