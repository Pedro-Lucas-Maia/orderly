package bti.pds.dinner.insights.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DemandForecastCalculator {
    public static final String METHOD = "WEEKDAY_AVERAGE";

    private DemandForecastCalculator() {
    }

    public static List<ProductDemand> forecast(
            List<CatalogProduct> products,
            List<LocalDate> sampleDates,
            List<DailySoldQuantity> history,
            int minSamplesHigh,
            int minSamplesMedium
    ) {
        Map<String, Integer> sold = new HashMap<>();
        for (DailySoldQuantity row : history) {
            sold.merge(key(row.date(), row.productId()), row.quantity(), Integer::sum);
        }

        List<ProductDemand> result = new ArrayList<>();
        for (CatalogProduct product : products) {
            int total = 0;
            for (LocalDate date : sampleDates) {
                total += sold.getOrDefault(key(date, product.id()), 0);
            }

            int sampleSize;
            int predicted;
            ForecastConfidence confidence;
            if (sampleDates.isEmpty()) {
                sampleSize = 0;
                predicted = 0;
                confidence = ForecastConfidence.LOW;
            } else if (total == 0) {
                sampleSize = 0;
                predicted = 0;
                confidence = ForecastConfidence.LOW;
            } else {
                sampleSize = sampleDates.size();
                predicted = (int) Math.round(total / (double) sampleDates.size());
                confidence = confidenceOf(sampleSize, minSamplesHigh, minSamplesMedium);
            }
            result.add(new ProductDemand(
                    product.id(),
                    product.name(),
                    product.price(),
                    predicted,
                    METHOD,
                    sampleSize,
                    confidence
            ));
        }
        return result;
    }

    private static ForecastConfidence confidenceOf(int sampleSize, int minHigh, int minMedium) {
        if (sampleSize >= minHigh) {
            return ForecastConfidence.HIGH;
        }
        if (sampleSize >= minMedium) {
            return ForecastConfidence.MEDIUM;
        }
        return ForecastConfidence.LOW;
    }

    private static String key(LocalDate date, Long productId) {
        return date + ":" + productId;
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
}
