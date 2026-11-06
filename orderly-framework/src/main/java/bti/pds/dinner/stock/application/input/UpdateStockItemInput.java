package bti.pds.dinner.stock.application.input;

import java.math.BigDecimal;

public record UpdateStockItemInput(
        String name,
        String category,
        Integer minimumStock,
        BigDecimal unitCost,
        Boolean active
) {
}
