package bti.pds.dinner.inventory.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Inventory {
    private InventoryId id;
    private List<InventoryItem> items;

    public Inventory(InventoryId id, List<InventoryItem> items){
        this.id = id;
        this.items = items;
    }

    public void addItem(Product product, int quantity){
        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        items.add(new InventoryItem(product, quantity));
    }

    public void removeItem(Product product){
        InventoryItem item = findItem(product);

        items.remove(item);
    }

    public void increaseQuantity(Product product, int quantity){
        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than zero");
        } 
        InventoryItem item = findItem(product);
        item.increaseQuantity(quantity);
    }

    public void decreaseQuantity(Product product, int quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        InventoryItem item = findItem(product);

        item.decreaseQuantity(quantity);
    }

    private InventoryItem findItem(Product product){
        return items.stream()
            .filter(item -> item.getProduct()
                .getProductId()
                .equals(product.getProductId()))
            .findFirst()
            .orElseThrow();
    }
}
