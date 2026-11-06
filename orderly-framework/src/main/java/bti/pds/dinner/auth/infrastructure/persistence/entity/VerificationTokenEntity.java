package bti.pds.dinner.auth.infrastructure.persistence.entity;

import bti.pds.dinner.auth.domain.UserId;
import bti.pds.dinner.auth.domain.VerificationToken;
import bti.pds.dinner.auth.domain.VerificationTokenId;
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
@Table(name = "verification_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationTokenEntity {
    @Id
    private UUID id;
    private String token;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    public static VerificationTokenEntity from(@NonNull VerificationToken verificationToken) {
        return new VerificationTokenEntity(
                verificationToken.getUserId().uuid(),
                verificationToken.getToken(),
                verificationToken.getUserId().uuid(),
                verificationToken.getExpiryDate()
        );
    }
    public static VerificationToken toDomain(@NonNull VerificationTokenEntity entity) {
        return new VerificationToken(
                new VerificationTokenId(entity.getId()),
                entity.getToken(),
                new UserId(entity.getUserId()),
                entity.getExpiryDate()
        );
    }
}
