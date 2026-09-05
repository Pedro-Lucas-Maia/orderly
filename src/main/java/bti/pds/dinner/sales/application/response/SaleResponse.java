package bti.pds.dinner.sales.application.response;

import java.math.BigDecimal;

public record SaleResponse(
    String saleId,
    BigDecimal totalAmount
) 
{}
