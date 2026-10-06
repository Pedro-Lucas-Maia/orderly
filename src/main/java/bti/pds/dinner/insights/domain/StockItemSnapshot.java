package bti.pds.dinner.insights.domain;

public record StockItemSnapshot(
        Long id,
        String name,
        String unit,
        int currentQuantity,
        int minimumStock
) {
}
