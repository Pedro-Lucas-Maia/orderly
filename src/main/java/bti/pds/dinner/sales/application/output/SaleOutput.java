package bti.pds.dinner.sales.application.output;

import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleStatus;
import org.jspecify.annotations.NonNull;

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
    public static SaleOutput from(@NonNull Sale sale, LocalDateTime referenceDate) {
        return new SaleOutput(
                sale.getId().toString(),
                referenceDate,
                sale.getStatus(),
                sale.calculateTotal(),
                sale.getObservation(),
                sale.getItems()
                        .stream()
                        .map(SaleItemOutput::from)
                        .toList()
        );
    }
}
