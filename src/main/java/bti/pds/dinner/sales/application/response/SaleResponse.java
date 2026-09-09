package bti.pds.dinner.sales.application.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import bti.pds.dinner.sales.domain.SaleStatus;

public record SaleResponse(
    String saleId,
    LocalDateTime date,
    SaleStatus status,
    BigDecimal totalAmount,
    String observation,
    List<SaleItemResponse> items
) 
{}
