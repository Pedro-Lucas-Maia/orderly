package bti.pds.dinner.auth.infrastructure.persistence.repository;

import bti.pds.dinner.auth.infrastructure.persistence.entity.UserEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEntityRepository extends CrudRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    @NonNull List<UserEntity> findAll();
    boolean existsByCpf(String cpf);
}
