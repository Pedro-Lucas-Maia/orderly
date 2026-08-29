package bti.pds.dinner.inventory.infrastructure.persistence.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
public class InventoryEntity {
    @Id
    private UUID id;

    @OneToMany(
        mappedBy = "inventory",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<InventoryItemEntity> items;
}
