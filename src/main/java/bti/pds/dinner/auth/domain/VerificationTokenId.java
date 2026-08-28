package bti.pds.dinner.auth.domain;

import java.util.UUID;

public record VerificationTokenId(UUID uuid) {
    public VerificationTokenId() {
        this(UUID.randomUUID());
    }
}
