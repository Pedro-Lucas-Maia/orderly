package bti.pds.dinner.inventory.infrastructure.http.response;

import java.util.UUID;

public record InventoryItemResponse(
    UUID productId,
    String productName,
    double price,
    String description,
    int quantity) {
}