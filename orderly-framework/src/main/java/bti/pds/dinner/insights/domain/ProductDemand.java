package bti.pds.dinner.insights.domain;

import java.math.BigDecimal;

public record ProductDemand(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int predictedQuantity,
        String method,
        int sampleSize,
        ForecastConfidence confidence
) {
}
