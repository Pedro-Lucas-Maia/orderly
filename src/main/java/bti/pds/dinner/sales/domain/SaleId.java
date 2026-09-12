package bti.pds.dinner.sales.domain;

import java.util.UUID;

public record SaleId(UUID uuid) {
    public SaleId() {
        this(UUID.randomUUID());
    }    
}
