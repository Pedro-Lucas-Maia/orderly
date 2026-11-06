package bti.pds.dinner.auth.infrastructure.persistence.repository;


import bti.pds.dinner.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PasswordResetTokenEntityRepository extends CrudRepository<PasswordResetTokenEntity, UUID> {

    java.util.Optional<PasswordResetTokenEntity> findByToken(String token);
    void deleteByUserId(UUID userId);
}
