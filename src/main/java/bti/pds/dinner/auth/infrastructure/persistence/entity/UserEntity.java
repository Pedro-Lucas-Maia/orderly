package bti.pds.dinner.auth.infrastructure.persistence.entity;

import bti.pds.dinner.auth.domain.RoleId;
import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private UUID id;

    private String name;

    private String cpf;

    private String email;

    private String password;

    @Column(name = "role_id")
    private UUID roleId;

    private boolean locked;

    private boolean enabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    @Column(name = "lockout_moment")
    private LocalDateTime lockoutMoment;

    public static UserEntity from(@NonNull User user) {
        return UserEntity.builder()
                .id(user.getId().uuid())
                .name(user.getName())
                .cpf(user.getCpf())
                .email(user.getEmail())
                .password(user.getPassword())
                .roleId(user.getRoleId().uuid())
                .locked(user.isLocked())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockoutMoment(user.getLockoutMoment())
                .build();
    }
    public static User toDomain(@NonNull UserEntity userEntity) {
       return User.builder()
               .id(new UserId(userEntity.getId()))
               .name(userEntity.getName())
               .cpf(userEntity.getCpf())
               .email(userEntity.getEmail())
               .password(userEntity.getPassword())
               .roleId(new RoleId(userEntity.getRoleId()))
               .locked(userEntity.isLocked())
               .enabled(userEntity.isEnabled())
               .createdAt(userEntity.getCreatedAt())
               .updatedAt(userEntity.getUpdatedAt())
               .lastLoginAt(userEntity.getLastLoginAt())
               .failedLoginAttempts(userEntity.getFailedLoginAttempts())
               .lockoutMoment(userEntity.getLockoutMoment())
               .build();
    }
}
