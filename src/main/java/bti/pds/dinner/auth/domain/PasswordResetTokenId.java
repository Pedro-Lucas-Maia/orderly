package bti.pds.dinner.auth.domain;

import java.util.UUID;

public record PasswordResetTokenId(UUID uuid) {
    public PasswordResetTokenId() {
        this(UUID.randomUUID());
    }
}
