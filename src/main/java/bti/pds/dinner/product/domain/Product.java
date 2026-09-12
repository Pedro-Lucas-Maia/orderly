package bti.pds.dinner.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class Product {
    private ProductId id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;
    private LocalDateTime deletedAt;

    public Product(String name, String description, BigDecimal price, boolean active) {
        this.id = null;
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Product update(String name, String description, BigDecimal price, Boolean active) {
        return Product.builder()
                .id(this.id)
                .name(name != null ? name : this.name)
                .description(description != null ? description : this.description)
                .price(price != null ? price : this.price)
                .active(active != null ? active : this.active)
                .deletedAt(this.deletedAt)
                .build();
    }

    public Product markDeleted() {
        return Product.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .active(false)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
