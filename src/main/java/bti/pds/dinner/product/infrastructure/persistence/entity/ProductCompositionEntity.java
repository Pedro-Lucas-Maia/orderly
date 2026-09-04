package bti.pds.dinner.product.infrastructure.persistence.entity;

import bti.pds.dinner.inventory.domain.StockItemId;
import bti.pds.dinner.product.domain.ProductComposition;
import bti.pds.dinner.product.domain.ProductCompositionId;
import bti.pds.dinner.product.domain.ProductId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Entity
@Table(name = "product_compositions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCompositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "stock_item_id")
    private Long stockItemId;

    private int quantity;

    public static ProductCompositionEntity from(@NonNull ProductComposition composition) {
        return ProductCompositionEntity.builder()
                .id(composition.getId() != null ? composition.getId().value() : null)
                .productId(composition.getProductId().value())
                .stockItemId(composition.getStockItemId().value())
                .quantity(composition.getQuantity())
                .build();
    }

    public static ProductComposition toDomain(@NonNull ProductCompositionEntity entity) {
        return ProductComposition.builder()
                .id(new ProductCompositionId(entity.getId()))
                .productId(new ProductId(entity.getProductId()))
                .stockItemId(new StockItemId(entity.getStockItemId()))
                .quantity(entity.getQuantity())
                .build();
    }
}
