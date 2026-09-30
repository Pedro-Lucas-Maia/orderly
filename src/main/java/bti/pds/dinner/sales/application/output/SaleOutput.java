package bti.pds.dinner.sales.application.output;

import bti.pds.dinner.sales.domain.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleOutput(
        String saleId,
        Long storeId,
        LocalDateTime date,
        SaleStatus status,
        BigDecimal totalAmount,
        String observation,
        List<SaleItemOutput> items,
        UUID userId,
        BigDecimal deliveryFee,
        String deliveryStreet,
        String deliveryNumber,
        String deliveryCity,
        String deliveryNeighborhood,
        String deliveryZipCode
) {
}
