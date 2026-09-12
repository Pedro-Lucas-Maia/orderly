package bti.pds.dinner.sales.domain;

import java.util.UUID;

public record SaleItemId(
        UUID uuid
) {
    public SaleItemId() {
        this(UUID.randomUUID());
    }
}
