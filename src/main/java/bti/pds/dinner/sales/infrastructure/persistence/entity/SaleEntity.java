package bti.pds.dinner.sales.infrastructure.persistence.entity;

import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleId;
import bti.pds.dinner.sales.domain.SaleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor 
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sales")
public class SaleEntity {
    @Id 
    private UUID id;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SaleStatus status;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Column(name = "observation")
    private String observation;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItemEntity> items = new ArrayList<>();

    public SaleEntity(UUID id, LocalDateTime date, SaleStatus status, BigDecimal totalValue, String observation) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.totalValue = totalValue;
        this.observation = observation;
    }

    public void addItem(@NonNull SaleItemEntity item){
        item.setSale(this);
        this.items.add(item);
    }

    public static Sale toDomain(@NonNull SaleEntity entity) {
        return new Sale(
                new SaleId(entity.getId()),
                entity.getDate(),
                entity.getStatus(),
                entity.items.stream()
                        .map(SaleItemEntity::toDomain)
                        .toList(),
                entity.getObservation()
        );
    }
    
    public static SaleEntity from(@NonNull Sale sale) {
        SaleEntity entity = new SaleEntity(
                sale.getId().uuid(),
                sale.getDate(),
                sale.getStatus(),
                sale.calculateTotal(),
                sale.getObservation()
        );
        if (sale.getItems() != null) {
            sale.getItems().forEach(item -> {
                SaleItemEntity itemEntity = new SaleItemEntity(
                        item.getId().uuid(),
                        entity,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                );
                entity.addItem(itemEntity);
            });
        }
        return entity;
    }
}
