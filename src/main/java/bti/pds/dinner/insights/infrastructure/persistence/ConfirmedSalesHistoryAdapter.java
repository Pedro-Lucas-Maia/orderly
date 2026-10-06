package bti.pds.dinner.insights.infrastructure.persistence;

import bti.pds.dinner.insights.domain.ConfirmedSalesHistory;
import bti.pds.dinner.insights.domain.DailySoldQuantity;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfirmedSalesHistoryAdapter implements ConfirmedSalesHistory {
    private final InsightsSaleQueryRepository queryRepository;

    public ConfirmedSalesHistoryAdapter(InsightsSaleQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Override
    public List<DailySoldQuantity> findDailyQuantities(Long storeId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        List<Object[]> rows = storeId == null
                ? queryRepository.aggregateAllStores(fromInclusive, toExclusive)
                : queryRepository.aggregateByStore(storeId, fromInclusive, toExclusive);
        List<DailySoldQuantity> result = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            Long productId = ((Number) row[1]).longValue();
            int quantity = ((Number) row[2]).intValue();
            result.add(new DailySoldQuantity(date, productId, quantity));
        }
        return result;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
