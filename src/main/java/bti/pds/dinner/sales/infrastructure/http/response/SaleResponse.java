package bti.pds.dinner.sales.infrastructure.http.response;

import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.SaleStatus;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
    String saleId,
    LocalDateTime date,
    SaleStatus status,
    BigDecimal totalAmount,
    String observation,
    List<SaleItemResponse> items
) 
{
    public static SaleResponse from(@NonNull SaleOutput output) {
        return new SaleResponse(
                output.saleId(),
                output.date(),
                output.status(),
                output.totalAmount(),
                output.observation(),
                output.items()
                        .stream()
                        .map(SaleItemResponse::from)
                        .toList()
        );
    }
}
