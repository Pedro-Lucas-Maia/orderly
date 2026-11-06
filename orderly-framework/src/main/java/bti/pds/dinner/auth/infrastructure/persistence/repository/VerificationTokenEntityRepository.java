package bti.pds.dinner.auth.infrastructure.persistence.repository;


import bti.pds.dinner.auth.infrastructure.persistence.entity.VerificationTokenEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenEntityRepository extends ListCrudRepository<VerificationTokenEntity, UUID> {
    Optional<VerificationTokenEntity> findByToken(String token);
}
