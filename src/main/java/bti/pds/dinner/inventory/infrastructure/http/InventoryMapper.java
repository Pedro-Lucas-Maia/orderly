package bti.pds.dinner.inventory.infrastructure.http;

import java.util.List;

import org.springframework.stereotype.Component;

import bti.pds.dinner.inventory.domain.Inventory;
import bti.pds.dinner.inventory.domain.InventoryItem;
import bti.pds.dinner.inventory.domain.Product;
import bti.pds.dinner.inventory.infrastructure.http.response.InventoryItemResponse;
import bti.pds.dinner.inventory.infrastructure.http.response.InventoryResponse;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {
        List<InventoryItemResponse> items = inventory.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new InventoryResponse(
                inventory.getId().uuid(),
                items);
    }

    private InventoryItemResponse toItemResponse(InventoryItem item) {
        Product product = item.getProduct();

        return new InventoryItemResponse(
                product.getProductId().uuid(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                item.getQuantity());
    }
}