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

    public void increaseQuantity(int amount){
        if (amount <= 0){
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        quantity += amount;
    }

    public void decreaseQuantity(int amount){
        if (amount <= 0){
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        quantity -= amount;
    }
}
