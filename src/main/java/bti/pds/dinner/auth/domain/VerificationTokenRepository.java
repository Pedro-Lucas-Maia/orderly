package bti.pds.dinner.auth.domain;

import java.util.Optional;

public interface VerificationTokenRepository {
    Optional<VerificationToken> findByToken(String token);
    VerificationToken save(VerificationToken verificationToken);
    void delete(VerificationToken verificationToken);
}
