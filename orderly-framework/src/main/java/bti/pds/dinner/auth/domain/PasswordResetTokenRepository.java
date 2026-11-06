package bti.pds.dinner.auth.domain;

import java.util.Optional;

public interface PasswordResetTokenRepository {
    Optional<PasswordResetToken> findById(PasswordResetTokenId id);
    Optional<PasswordResetToken> findByToken(String token);
    PasswordResetToken save(PasswordResetToken passwordResetToken);
    void delete(PasswordResetToken passwordResetToken);
    void deleteByUserId(UserId userId);
}
