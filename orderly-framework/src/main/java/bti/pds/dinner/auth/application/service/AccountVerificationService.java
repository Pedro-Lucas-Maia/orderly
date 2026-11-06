package bti.pds.dinner.auth.application.service;


import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.domain.VerificationToken;
import bti.pds.dinner.auth.domain.VerificationTokenRepository;
import bti.pds.dinner.auth.domain.exception.TokenNotValidException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountVerificationService {
    private final static int HOURS_TO_VERIFY = 24;
    private final VerificationTokenRepository verificationTokenRepository;

    public AccountVerificationService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Transactional
    public String createVerificationToken(@NonNull User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken newToken = new VerificationToken(token, user.getId(), LocalDateTime.now().plusHours(HOURS_TO_VERIFY));

        return verificationTokenRepository.save(newToken).getToken();
    }

    @Transactional
    public UUID verifyToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new TokenNotValidException("Token with the value " + token + " not valid"));
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenNotValidException("Token with the value " + token + " not valid");
        }
        verificationTokenRepository.delete(verificationToken);
        return verificationToken.getUserId().uuid();
    }

}
