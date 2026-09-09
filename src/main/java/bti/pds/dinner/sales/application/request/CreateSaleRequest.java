package bti.pds.dinner.sales.application.request;

import java.util.List;

public record CreateSaleRequest(
    String observation,
    List<SaleItemRequest> items
) 
{}
