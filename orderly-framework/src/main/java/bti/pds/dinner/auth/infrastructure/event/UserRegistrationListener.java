package bti.pds.dinner.auth.infrastructure.event;


import bti.pds.dinner.auth.application.service.MailService;
import bti.pds.dinner.auth.domain.event.OnUserRegisteredEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegistrationListener {
    private final MailService mailService;

    public UserRegistrationListener(MailService mailService) {
        this.mailService = mailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserRegistration(OnUserRegisteredEvent event) {
        mailService.sendVerificationEmail(event.user(), event.token());
    }
}
