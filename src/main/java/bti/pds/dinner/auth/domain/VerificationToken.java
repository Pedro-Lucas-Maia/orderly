package bti.pds.dinner.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VerificationToken {
    private VerificationTokenId id;
    private String token;
    private UserId userId;
    private LocalDateTime expiryDate;

    public VerificationToken(String token, UserId userId, LocalDateTime expiryDate) {
        this.id = new VerificationTokenId();
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }
}
