package bti.pds.dinner.stock.domain;

public record StockItemFilter(
        String name,
        Boolean active,
        String category
) {
}
