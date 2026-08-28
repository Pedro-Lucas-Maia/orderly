package bti.pds.dinner.auth.domain;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findById(RoleId id);
    Optional<Role> findByName(String name);
    Role save(Role role);
    List<Role> findAll();
}
