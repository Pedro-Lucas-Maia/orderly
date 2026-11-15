package bti.pds.dinner.auth.infrastructure.event;

import bti.pds.dinner.auth.application.service.MailService;
import bti.pds.dinner.auth.domain.event.OnPasswordResetedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class PasswordResetListener {
    private final MailService mailService;

    public PasswordResetListener(MailService mailService) {
        this.mailService = mailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Observed(name = "event.user.password-reset.async")
    public void handlePasswordReset(OnPasswordResetedEvent event) {
        var userId = event.user().getId().uuid();
        try {
            mailService.sendResetPasswordEmail(event.user(), event.token());
        } catch (Exception e) {
            log.error("Async event processing failed: could not send password reset email. event=OnPasswordResetEvent, userId={}",
                    userId, e);
        }

    }
}
