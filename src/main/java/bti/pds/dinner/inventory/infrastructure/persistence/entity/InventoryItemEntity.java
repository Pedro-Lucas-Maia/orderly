package bti.pds.dinner.inventory.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
public class InventoryItemEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private InventoryEntity inventory;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    private int quantity;
}
