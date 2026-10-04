package bti.pds.dinner.insights.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface ConfirmedSalesHistory {
    List<DailySoldQuantity> findDailyQuantities(Long storeId, LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
