package bti.pds.dinner.inventory.domain;

import java.util.UUID;

public record ProductId(UUID uuid){
    public ProductId(){
        this(UUID.randomUUID());
    }
}