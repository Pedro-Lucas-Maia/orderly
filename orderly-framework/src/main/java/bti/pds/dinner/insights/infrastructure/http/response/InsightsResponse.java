package bti.pds.dinner.insights.infrastructure.http.response;

import bti.pds.dinner.insights.application.output.DailyInsightsOutput;
import bti.pds.dinner.insights.domain.ForecastConfidence;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record InsightsResponse(
        Long storeId,
        String storeName,
        LocalDate targetDate,
        DayOfWeek weekday,
        int horizonDays,
        int expiryWindowDays,
        Assumptions assumptions,
        List<ProductDemand> productDemand,
        List<PurchaseSuggestion> purchaseSuggestions,
        List<CriticalStock> criticalStock,
        List<ExpiringLot> expiringLots,
        List<PromotionSuggestion> promotionSuggestions
) {
    public static InsightsResponse from(@NonNull DailyInsightsOutput output) {
        return new InsightsResponse(
                output.storeId(),
                output.storeName(),
                output.targetDate(),
                output.weekday(),
                output.horizonDays(),
                output.expiryWindowDays(),
                new Assumptions(
                        output.assumptions().stockScope(),
                        output.assumptions().demandScope(),
                        output.assumptions().forecastMethod(),
                        output.assumptions().forecastWeeks(),
                        output.assumptions().note()
                ),
                output.productDemand().stream().map(row -> new ProductDemand(
                        row.productId(), row.productName(), row.unitPrice(), row.predictedQuantity(),
                        row.method(), row.sampleSize(), row.confidence()
                )).toList(),
                output.purchaseSuggestions().stream().map(row -> new PurchaseSuggestion(
                        row.stockItemId(), row.name(), row.unit(), row.currentQuantity(), row.minimumStock(),
                        row.predictedConsumption(), row.quantityToBuy(), row.belowMinimum(), row.affectedProductIds()
                )).toList(),
                output.criticalStock().stream().map(row -> new CriticalStock(
                        row.stockItemId(), row.name(), row.unit(), row.currentQuantity(),
                        row.minimumStock(), row.affectedProductIds()
                )).toList(),
                output.expiringLots().stream().map(row -> new ExpiringLot(
                        row.lotId(), row.stockItemId(), row.stockItemName(), row.quantity(),
                        row.expiresAt(), row.daysUntilExpiry()
                )).toList(),
                output.promotionSuggestions().stream().map(row -> new PromotionSuggestion(
                        row.stockItemId(), row.lotId(), row.expiresAt(), row.quantityInLot(),
                        row.suggestedProducts().stream().map(product -> new SuggestedProduct(
                                product.productId(), product.productName(), product.predictedQuantityToday()
                        )).toList()
                )).toList()
        );
    }

    public record Assumptions(
            String stockScope,
            String demandScope,
            String forecastMethod,
            int forecastWeeks,
            String note
    ) {
    }

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

    public record PurchaseSuggestion(
            Long stockItemId,
            String name,
            String unit,
            int currentQuantity,
            int minimumStock,
            int predictedConsumption,
            int quantityToBuy,
            boolean belowMinimum,
            List<Long> affectedProductIds
    ) {
    }

    public record CriticalStock(
            Long stockItemId,
            String name,
            String unit,
            int currentQuantity,
            int minimumStock,
            List<Long> affectedProductIds
    ) {
    }

    public record ExpiringLot(
            Long lotId,
            Long stockItemId,
            String stockItemName,
            int quantity,
            LocalDate expiresAt,
            long daysUntilExpiry
    ) {
    }

    public record PromotionSuggestion(
            Long stockItemId,
            Long lotId,
            LocalDate expiresAt,
            int quantityInLot,
            List<SuggestedProduct> suggestedProducts
    ) {
    }

    public record SuggestedProduct(
            Long productId,
            String productName,
            int predictedQuantityToday
    ) {
    }
}
