package bti.pds.dinner.auth.infrastructure.persistence.repository;

import bti.pds.dinner.auth.domain.Role;
import bti.pds.dinner.auth.domain.RoleId;
import bti.pds.dinner.auth.domain.RoleRepository;
import bti.pds.dinner.auth.infrastructure.persistence.entity.RoleEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaRoleRepository implements RoleRepository {
    private final RoleEntityRepository repository;

    public JpaRoleRepository(RoleEntityRepository roleEntityRepository) {
        this.repository = roleEntityRepository;
    }


    @Override
    public Optional<Role> findById(@NonNull RoleId id) {
        return repository.findById(id.uuid())
                .map(RoleEntity::toDomain);
    }

    @Override
    public Optional<Role> findByName(@NonNull String name) {
        return repository.findByName(name)
                .map(RoleEntity::toDomain);
    }

    @Override
    public Role save(Role role) {
        return RoleEntity.toDomain(repository.save(RoleEntity.from(role)));
    }

    @Override
    public List<Role> findAll() {
        return repository.findAll()
                .stream()
                .map(RoleEntity::toDomain)
                .toList();
    }
}
