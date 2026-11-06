package bti.pds.dinner.auth.infrastructure.event;

import bti.pds.dinner.auth.application.service.MailService;
import bti.pds.dinner.auth.domain.event.OnPasswordResetedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetListener {
    private final MailService mailService;

    public PasswordResetListener(MailService mailService) {
        this.mailService = mailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePasswordReset(OnPasswordResetedEvent event) {
        mailService.sendResetPasswordEmail(event.user(), event.token());
    }
}
