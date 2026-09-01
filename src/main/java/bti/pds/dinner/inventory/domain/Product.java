package bti.pds.dinner.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Product {
    private ProductId productId;
    private String name;
    private double price;
    private String description;
}
