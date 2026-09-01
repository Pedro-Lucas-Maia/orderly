package bti.pds.dinner.inventory.infrastructure.http.request;

import java.util.UUID;

public record AddInventoryItemRequest(
    UUID productId, int quantity
) {}
