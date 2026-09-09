package bti.pds.dinner.sales.infrastructure.persistence.entity;

import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleID;
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
    private String id;

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
    private List<SaleItemEntity> itens = new ArrayList<>();

    public SaleEntity(String id, LocalDateTime date, SaleStatus status, BigDecimal totalValue, String observation) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.totalValue = totalValue;
        this.observation = observation;
    }

    public void addItem(@NonNull SaleItemEntity item){
        item.setSale(this);
        this.itens.add(item);
    }

    public static Sale toDomain(@NonNull SaleEntity entity) {
        return new Sale(
                new SaleID(UUID.fromString(entity.getId())),
                entity.getDate(),
                entity.getStatus(),
                entity.itens.stream()
                        .map(SaleItemEntity::toDomain)
                        .toList(),
                entity.observation

        );
    }
    public static SaleEntity from(@NonNull Sale sale) {
        return new SaleEntity(
                sale.getId().uuid().toString(),
                sale.getDate(),
                sale.getStatus(),
                sale.calculateTotal(),
                sale.getObservation()
        );
    }
}
