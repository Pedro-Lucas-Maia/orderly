package bti.pds.dinner.stock.application.input;

public record ListStockItemsInput(
        Boolean active,
        String name,
        String category,
        int page,
        int size
) {
    public ListStockItemsInput {
        if (page < 0) page = 0;
        if (size < 0) size = 10;
    }
}
