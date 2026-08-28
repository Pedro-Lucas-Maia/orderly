package bti.pds.dinner.auth.infrastructure.persistence.repository;

import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.domain.UserId;
import bti.pds.dinner.auth.domain.UserRepository;
import bti.pds.dinner.auth.infrastructure.persistence.entity.UserEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserRepository implements UserRepository {
    private final UserEntityRepository repository;

    public JpaUserRepository(UserEntityRepository userEntityRepository) {
        this.repository = userEntityRepository;
    }


    @Override
    public Optional<User> findById(@NonNull UserId userId) {
        return repository.findById(userId.uuid())
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        return UserEntity.toDomain(repository.save(UserEntity.from(user)));
    }

    @Override
    public List<User> findAll() {
        return repository.findAll()
                .stream()
                .map(UserEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(User user) {
        repository.delete(UserEntity.from(user));
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
