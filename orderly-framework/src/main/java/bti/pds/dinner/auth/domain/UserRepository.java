package bti.pds.dinner.auth.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId userId);
    Optional<User> findByEmail(String email);
    User save(User user);
    List<User> findAll();
    void delete(User user);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
