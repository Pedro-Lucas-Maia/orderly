package bti.pds.dinner.auth.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PasswordResetToken {
    private PasswordResetTokenId id;
    private String token;
    private UserId userId;
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, UserId userId, LocalDateTime expiryDate) {
        this.id = new PasswordResetTokenId();
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }

    public void validate() {
        if (this.expiryDate.isBefore(LocalDateTime.now())) {
            throw new bti.pds.dinner.auth.domain.exception.TokenNotValidException("Invalid token");
        }
    }
}
