package bti.pds.dinner.reports.infrastructure.persistence.adapter;

import bti.pds.dinner.reports.domain.sales.DailyMetric;
import bti.pds.dinner.reports.domain.sales.ProductMetric;
import bti.pds.dinner.reports.domain.sales.SalesReportDataPort;
import bti.pds.dinner.reports.domain.sales.StatusMetric;
import bti.pds.dinner.reports.infrastructure.persistence.repository.JdbcSalesReportRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SalesReportDataAdapter implements SalesReportDataPort {

    private final JdbcSalesReportRepository jdbcRepository;

    public SalesReportDataAdapter(JdbcSalesReportRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public List<StatusMetric> findStatusMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcRepository.aggregateByStatus(fromInclusive, toExclusive).stream()
                .map(row -> new StatusMetric(
                        row.status(),
                        row.orderCount(),
                        row.totalAmount(),
                        row.deliveryFeeAmount()
                ))
                .toList();
    }

    @Override
    public List<DailyMetric> findDailyMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcRepository.aggregateDailyDelivered(fromInclusive, toExclusive).stream()
                .map(row -> new DailyMetric(
                        row.saleDate(),
                        row.deliveredOrders(),
                        row.dailyRevenue()
                ))
                .toList();
    }

    @Override
    public List<ProductMetric> findProductMetrics(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcRepository.aggregateTopProducts(fromInclusive, toExclusive).stream()
                .map(row -> new ProductMetric(
                        row.productId(),
                        row.productName(),
                        row.quantitySold(),
                        row.revenueGenerated()
                ))
                .toList();
    }
}