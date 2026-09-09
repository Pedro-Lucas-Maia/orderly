package bti.pds.dinner.sales.application.input;

public record SaleItemInput(
    String productId,
    int quantity
) {
}
