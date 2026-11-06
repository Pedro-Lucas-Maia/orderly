package bti.pds.dinner.reports.infrastructure.persistence.adapter;

import bti.pds.dinner.reports.domain.stock.ItemMovementMetric;
import bti.pds.dinner.reports.domain.stock.MovementTypeMetric;
import bti.pds.dinner.reports.domain.stock.StockItemSnapshot;
import bti.pds.dinner.reports.domain.stock.StockReportDataPort;
import bti.pds.dinner.reports.infrastructure.persistence.repository.JdbcStockReportRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockReportDataAdapter implements StockReportDataPort {

    private final JdbcStockReportRepository jdbcRepository;

    public StockReportDataAdapter(JdbcStockReportRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public List<StockItemSnapshot> findActiveStockItems() {
        return jdbcRepository.findActiveStockItems().stream()
                .map(row -> new StockItemSnapshot(
                        row.id(),
                        row.name(),
                        row.category(),
                        row.unit(),
                        row.currentQuantity(),
                        row.minimumStock(),
                        row.unitCost()
                ))
                .toList();
    }

    @Override
    public List<MovementTypeMetric> findMovementTypeMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcRepository.aggregateMovementsByType(fromInclusive, toExclusive).stream()
                .map(row -> new MovementTypeMetric(
                        row.type(),
                        row.movementsCount(),
                        row.totalQuantity(),
                        row.estimatedFinancialValue()
                ))
                .toList();
    }

    @Override
    public List<ItemMovementMetric> findItemMovementMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcRepository.aggregateMovementByItem(fromInclusive, toExclusive).stream()
                .map(row -> new ItemMovementMetric(
                        row.stockItemId(),
                        row.stockItemName(),
                        row.category(),
                        row.unit(),
                        row.type(),
                        row.totalQuantity(),
                        row.totalCost()
                ))
                .toList();
    }
}