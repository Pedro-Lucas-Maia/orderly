package bti.pds.dinner.sales.application.request;

public record SaleItemRequest(
    String productId,
    int quantity
) 
{}
