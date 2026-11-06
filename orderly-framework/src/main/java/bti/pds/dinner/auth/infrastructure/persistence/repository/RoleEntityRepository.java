package bti.pds.dinner.auth.infrastructure.persistence.repository;

import bti.pds.dinner.auth.infrastructure.persistence.entity.RoleEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleEntityRepository extends CrudRepository<RoleEntity, UUID> {
    @NonNull List<RoleEntity> findAll();
    Optional<RoleEntity> findByName(String name);
}
