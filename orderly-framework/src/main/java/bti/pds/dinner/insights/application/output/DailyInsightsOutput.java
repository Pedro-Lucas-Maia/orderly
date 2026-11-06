package bti.pds.dinner.insights.application.output;

import bti.pds.dinner.insights.domain.ForecastConfidence;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record DailyInsightsOutput(
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
