package bti.pds.dinner.auth.application.service;

import bti.pds.dinner.auth.application.input.ConfirmPasswordResetInput;
import bti.pds.dinner.auth.application.input.InitiatePasswordResetInput;
import bti.pds.dinner.auth.application.input.VerifyPasswordResetTokenInput;
import bti.pds.dinner.auth.domain.PasswordResetToken;
import bti.pds.dinner.auth.domain.PasswordResetTokenRepository;
import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.domain.UserRepository;
import bti.pds.dinner.auth.domain.event.OnPasswordResetedEvent;
import bti.pds.dinner.auth.domain.exception.TokenNotValidException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class PasswordResetService {
    private final static int HOURS_TO_RESET_PASSWORD = 1;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    private String createPasswordResetToken(@NonNull User user) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = new PasswordResetToken(
                token,
                user.getId(),
                LocalDateTime.now().plusHours(HOURS_TO_RESET_PASSWORD)
        );

        passwordResetTokenRepository.save(passwordResetToken);
        return token;
    }

    @Transactional
    @Observed(name = "auth.user.initiate-password-reset")
    public void initiatePasswordReset(@NonNull InitiatePasswordResetInput input) {
        User user = userRepository.findByEmail(input.email()).orElse(null);
        if (user == null) {
            return;
        }

        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = createPasswordResetToken(user);
        eventPublisher.publishEvent(new OnPasswordResetedEvent(user, token));
        log.info("Initiate password reset for user. userId={}", user.getId().uuid());
    }

    public void verifyToken(@NonNull VerifyPasswordResetTokenInput input) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(input.token())
                .orElseThrow(() -> new TokenNotValidException("Invalid token"));
        passwordResetToken.validate();
    }

    @Transactional
    @Observed(name = "auth.user.confirm-password-reset")
    public void confirmPasswordReset(@NonNull ConfirmPasswordResetInput input) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(input.token())
                .orElseThrow(() -> new TokenNotValidException("Invalid token"));
        passwordResetToken.validate();

        User user = userRepository.findById(passwordResetToken.getUserId())
                .orElseThrow(() -> new TokenNotValidException("Invalid token"));

        user.updatePassword(passwordEncoder.encode(input.newPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);
        log.info("Password was reset for user. userId={}", user.getId().uuid());
    }
}
