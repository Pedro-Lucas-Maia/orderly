package bti.pds.dinner.auth.infrastructure.persistence.entity;

import bti.pds.dinner.auth.domain.Role;
import bti.pds.dinner.auth.domain.RoleId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity {
    @Id
    private UUID id;
    private String name;

    public static RoleEntity from(@NonNull Role role) {
        return new RoleEntity(
                role.getId().uuid(),
                role.getName()
        );
    }
    public static Role toDomain(@NonNull RoleEntity roleEntity) {
        return new Role(
                new RoleId(roleEntity.getId()),
                roleEntity.getName()
        );
    }
}
