package bti.pds.dinner.sales.application.output;

import bti.pds.dinner.sales.domain.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleOutput(
        String saleId,
        LocalDateTime date,
        SaleStatus status,
        BigDecimal totalAmount,
        String observation,
        List<SaleItemOutput> items
) {
}
