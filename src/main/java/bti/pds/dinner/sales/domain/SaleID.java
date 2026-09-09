package bti.pds.dinner.sales.domain;

import java.util.UUID;

public record SaleID(UUID uuid) {
    public SaleID() {
        this(UUID.randomUUID());
    }    
}
