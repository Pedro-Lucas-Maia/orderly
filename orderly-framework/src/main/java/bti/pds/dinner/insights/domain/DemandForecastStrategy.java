package bti.pds.dinner.insights.domain;

import java.time.LocalDate;
import java.util.List;

public interface DemandForecastStrategy {
    String getMethodName();
    int getHistoryLookbackDays();
    List<ProductDemand> forecast(
            List<CatalogProduct> products,
            List<DailySoldQuantity> history,
            LocalDate targetDate
    );
}
