package bti.pds.dinner.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InventoryItem {
    Product product;
    int quantity;
}
