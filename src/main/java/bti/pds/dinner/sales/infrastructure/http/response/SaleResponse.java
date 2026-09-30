package bti.pds.dinner.sales.infrastructure.http.response;

import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.SaleStatus;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleResponse(
    String saleId,
    Long storeId,
    LocalDateTime date,
    SaleStatus status,
    BigDecimal totalAmount,
    String observation,
    List<SaleItemResponse> items,
    UUID userId,
    BigDecimal deliveryFee,
    String deliveryStreet,
    String deliveryNumber,
    String deliveryCity,
    String deliveryNeighborhood,
    String deliveryZipCode
) 
{
    public static SaleResponse from(@NonNull SaleOutput output) {
        return new SaleResponse(
                output.saleId(),
                output.storeId(),
                output.date(),
                output.status(),
                output.totalAmount(),
                output.observation(),
                output.items()
                        .stream()
                        .map(SaleItemResponse::from)
                        .toList(),
                output.userId(),
                output.deliveryFee(),
                output.deliveryStreet(),
                output.deliveryNumber(),
                output.deliveryCity(),
                output.deliveryNeighborhood(),
                output.deliveryZipCode()
        );
    }
}
