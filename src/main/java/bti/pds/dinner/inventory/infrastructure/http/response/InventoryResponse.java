package bti.pds.dinner.inventory.infrastructure.http.response;

import java.util.List;
import java.util.UUID;

public record InventoryResponse(UUID inventoryId, List<InventoryItemResponse> items) {}
