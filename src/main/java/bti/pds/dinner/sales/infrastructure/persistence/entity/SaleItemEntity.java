package bti.pds.dinner.sales.infrastructure.persistence.entity;

import bti.pds.dinner.sales.domain.SaleItem;
import bti.pds.dinner.sales.domain.SaleItemId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sale_items")
@AllArgsConstructor
@NoArgsConstructor
@Setter 
@Getter
public class SaleItemEntity {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleEntity sale;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    public static SaleItem toDomain(@NonNull SaleItemEntity entity) {
        return new SaleItem(
            new SaleItemId(UUID.fromString(entity.getId())),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }
}
