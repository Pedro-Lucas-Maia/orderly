package bti.pds.dinner.sales.application.input;

public record SaleItemInput(
    Long productId,
    int quantity
) {
}
