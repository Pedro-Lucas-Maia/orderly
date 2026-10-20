package bti.pds.dinner.reports.domain.sales;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesReportDataPort {
    List<StatusMetric> findStatusMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive);
    List<DailyMetric> findDailyMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive);
    List<ProductMetric> findProductMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
