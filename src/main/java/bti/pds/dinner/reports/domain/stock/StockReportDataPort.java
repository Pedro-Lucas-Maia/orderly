package bti.pds.dinner.reports.domain.stock;

import java.time.LocalDateTime;
import java.util.List;

public interface StockReportDataPort {
    List<StockItemSnapshot> findActiveStockItems();
    List<MovementTypeMetric> findMovementTypeMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive);
    List<ItemMovementMetric> findItemMovementMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
