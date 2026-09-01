package bti.pds.dinner.inventory.domain;

import java.util.UUID;

public record InventoryId(UUID uuid) {
    public InventoryId(){
        this(UUID.randomUUID());
    }
}
