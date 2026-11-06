package bti.pds.dinner.auth.infrastructure.persistence.entity;

import bti.pds.dinner.auth.domain.PasswordResetToken;
import bti.pds.dinner.auth.domain.PasswordResetTokenId;
import bti.pds.dinner.auth.domain.UserId;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetTokenEntity {
    @Id
    private UUID id;

    private String token;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    public static PasswordResetTokenEntity from(@NonNull PasswordResetToken passwordResetToken) {
        return new PasswordResetTokenEntity(
                passwordResetToken.getId().uuid(),
                passwordResetToken.getToken(),
                passwordResetToken.getUserId().uuid(),
                passwordResetToken.getExpiryDate()
        );
    }
    public static PasswordResetToken toDomain(@NonNull PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                new PasswordResetTokenId(entity.getId()),
                entity.getToken(),
                new UserId(entity.getUserId()),
                entity.getExpiryDate()
        );
    }
}
