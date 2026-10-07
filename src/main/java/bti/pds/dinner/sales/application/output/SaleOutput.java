package bti.pds.dinner.sales.application.output;

import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleStatus;
import org.jspecify.annotations.NonNull;

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
    public static SaleOutput from(@NonNull Sale sale) {
        return new SaleOutput(
                sale.getId().uuid().toString(),
                sale.getStoreId(),
                sale.getDate(),
                sale.getStatus(),
                sale.calculateTotal(sale.getDeliveryFee()),
                sale.getObservation(),
                sale.getItems().stream()
                        .map(SaleItemOutput::from)
                        .toList(),
                sale.getUserId().uuid(),
                sale.getDeliveryFee(),
                sale.getDeliveryStreet(),
                sale.getDeliveryNumber(),
                sale.getDeliveryNeighborhood(),
                sale.getDeliveryCity(),
                sale.getDeliveryZipCode()
        );
    }
}
