package bti.pds.dinner.insights.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BurgerDemandForecastStrategy implements DemandForecastStrategy {

    public static final String METHOD = "WEEKDAY_AVERAGE";

    private final int forecastWeeks;
    private final int minSamplesHigh;
    private final int minSamplesMedium;

    public BurgerDemandForecastStrategy() {
        this(8, 6, 3);
    }

    @Autowired
    public BurgerDemandForecastStrategy(
            @Value("${app.insights.forecast-weeks:8}") int forecastWeeks,
            @Value("${app.insights.min-samples-high:6}") int minSamplesHigh,
            @Value("${app.insights.min-samples-medium:3}") int minSamplesMedium
    ) {
        this.forecastWeeks = forecastWeeks;
        this.minSamplesHigh = minSamplesHigh;
        this.minSamplesMedium = minSamplesMedium;
    }

    @Override
    public String getMethodName() {
        return METHOD;
    }

    @Override
    public int getHistoryLookbackDays() {
        return forecastWeeks * 7;
    }

    @Override
    public List<ProductDemand> forecast(
            List<CatalogProduct> products,
            List<DailySoldQuantity> history,
            LocalDate targetDate
    ) {
        List<LocalDate> sampleDates = new ArrayList<>();
        for (int week = 1; week <= forecastWeeks; week++) {
            sampleDates.add(targetDate.minusWeeks(week));
        }

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
            if (sampleDates.isEmpty() || total == 0) {
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
}
