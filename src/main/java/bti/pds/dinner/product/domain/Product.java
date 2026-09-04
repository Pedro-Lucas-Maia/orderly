package bti.pds.dinner.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class Product {
    private ProductId id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;

    public Product(String name, String description, BigDecimal price, boolean active) {
        this.id = null;
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }
}
