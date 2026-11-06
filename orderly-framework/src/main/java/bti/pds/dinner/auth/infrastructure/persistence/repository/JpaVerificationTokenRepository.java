package bti.pds.dinner.auth.infrastructure.persistence.repository;

import bti.pds.dinner.auth.domain.VerificationToken;
import bti.pds.dinner.auth.domain.VerificationTokenRepository;
import bti.pds.dinner.auth.infrastructure.persistence.entity.VerificationTokenEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaVerificationTokenRepository implements VerificationTokenRepository {
    private final VerificationTokenEntityRepository repository;

    public JpaVerificationTokenRepository(VerificationTokenEntityRepository verificationTokenEntityRepository) {
        this.repository = verificationTokenEntityRepository;
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return repository.findByToken(token)
                .map(VerificationTokenEntity::toDomain);
    }

    @Override
    public VerificationToken save(VerificationToken verificationToken) {
        return VerificationTokenEntity.toDomain(repository.save(VerificationTokenEntity.from(verificationToken)));
    }

    @Override
    public void delete(VerificationToken verificationToken) {
        repository.delete(VerificationTokenEntity.from(verificationToken));
    }
}
