package bti.pds.dinner.inventory.application.service;

import org.springframework.stereotype.Service;

import bti.pds.dinner.inventory.domain.Inventory;
import bti.pds.dinner.inventory.domain.InventoryId;
import bti.pds.dinner.inventory.domain.InventoryRepository;
import bti.pds.dinner.inventory.domain.Product;
import bti.pds.dinner.inventory.domain.ProductId;
import bti.pds.dinner.inventory.domain.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public Inventory addItem(InventoryId inventoryId, ProductId productId, int quantity) {
        Inventory inventory = getInventory(inventoryId);

        Product product = productRepository.findById(productId).orElseThrow(
            () -> new IllegalArgumentException("Product not found.")
        );

        inventory.addItem(product, quantity);

        return inventoryRepository.save(inventory);
    }

    public Inventory removeItem(InventoryId inventoryId, ProductId productId) {
        Inventory inventory = getInventory(inventoryId);

        Product product = productRepository.findById(productId).orElseThrow(
            () -> new IllegalArgumentException("Product not found")
        );

        inventory.removeItem(product);

        return inventoryRepository.save(inventory);
    }

    public Inventory increaseQuantity(InventoryId inventoryId, ProductId productId, int quantity) {
        Inventory inventory = getInventory(inventoryId);

        Product product = productRepository.findById(productId).orElseThrow(
            () -> new IllegalArgumentException("Product not found.")
        );

        inventory.increaseQuantity(product, quantity);

        return inventoryRepository.save(inventory);
    }

    public Inventory decreaseQuantity(InventoryId inventoryId, ProductId productId, int quantity) {
        Inventory inventory = getInventory(inventoryId);

        Product product = productRepository.findById(productId).orElseThrow(
            () -> new IllegalArgumentException("Product not found.")
        );
        inventory.decreaseQuantity(product, quantity);

        return inventoryRepository.save(inventory);
    }

    public Inventory getInventory(InventoryId inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found."));
    }
}