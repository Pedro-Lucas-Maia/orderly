package bti.pds.dinner.auth.infrastructure.persistence.repository;

import bti.pds.dinner.auth.domain.PasswordResetToken;
import bti.pds.dinner.auth.domain.PasswordResetTokenId;
import bti.pds.dinner.auth.domain.PasswordResetTokenRepository;
import bti.pds.dinner.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaPassWordResetTokenRepository implements PasswordResetTokenRepository {
    private final PasswordResetTokenEntityRepository repository;

    public JpaPassWordResetTokenRepository(PasswordResetTokenEntityRepository passwordResetTokenEntityRepository) {
        this.repository = passwordResetTokenEntityRepository;
    }

    @Override
    public Optional<PasswordResetToken> findById(PasswordResetTokenId id) {
        return repository.findById(id.uuid())
                .map(PasswordResetTokenEntity::toDomain);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        return PasswordResetTokenEntity.toDomain(PasswordResetTokenEntity.from(passwordResetToken));
    }

    @Override
    public void delete(PasswordResetToken passwordResetToken) {
        repository.delete(PasswordResetTokenEntity.from(passwordResetToken));
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return repository.findByToken(token).map(PasswordResetTokenEntity::toDomain);
    }

    @Override
    public void deleteByUserId(bti.pds.dinner.auth.domain.UserId userId) {
        repository.deleteByUserId(userId.uuid());
    }
}
