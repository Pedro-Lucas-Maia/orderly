package bti.pds.dinner.product.infrastructure.persistence.entity;

import bti.pds.dinner.product.domain.Product;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static ProductEntity from(@NonNull Product product) {
        return ProductEntity.builder()
                .id(product.getId() != null ? product.getId().value() : null)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .active(product.isActive())
                .deletedAt(product.getDeletedAt())
                .build();
    }

    public static Product toDomain(@NonNull ProductEntity entity) {
        return Product.builder()
                .id(new ProductId(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .active(entity.isActive())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
