package bti.pds.dinner.inventory.application.input;

import java.math.BigDecimal;

public record CreateStockItemInput(
        String name,
        String category,
        String unit,
        int currentQuantity,
        int minimumStock,
        BigDecimal unitCost,
        boolean active
) {
}
