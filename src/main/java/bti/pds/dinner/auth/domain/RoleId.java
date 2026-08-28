package bti.pds.dinner.auth.domain;

import java.util.UUID;

public record RoleId(UUID uuid) {
    public RoleId() {
        this(UUID.randomUUID());
    }
}
