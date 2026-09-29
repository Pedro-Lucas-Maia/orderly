package bti.pds.dinner.address.domain;

import java.util.UUID;

public record AddressId(UUID uuid) {
    public AddressId() {
        this(UUID.randomUUID());
    }
}
