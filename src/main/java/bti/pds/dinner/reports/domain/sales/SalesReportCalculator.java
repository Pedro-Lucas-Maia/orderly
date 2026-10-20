package bti.pds.dinner.reports.domain.sales;

import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class SalesReportCalculator {

    private static final String STATUS_DELIVERED = "ENTREGUE";
    private static final String STATUS_CANCELED = "CANCELADA";
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public SalesSummary summarize(List<StatusMetric> statusMetrics) {
        long totalOrders = sumTotalOrders(statusMetrics);

        Optional<StatusMetric> deliveredMetric = findByStatus(statusMetrics, STATUS_DELIVERED);
        long deliveredOrders = deliveredMetric.map(StatusMetric::orderCount).orElse(0L);
        BigDecimal grossRevenue = deliveredMetric.map(StatusMetric::totalAmount).orElse(BigDecimal.ZERO);
        BigDecimal deliveryFeeTotal = deliveredMetric.map(StatusMetric::deliveryFeeAmount).orElse(BigDecimal.ZERO);

        long canceledOrders = findByStatus(statusMetrics, STATUS_CANCELED)
                .map(StatusMetric::orderCount)
                .orElse(0L);

        BigDecimal netProductRevenue = calculateNetProductRevenue(grossRevenue, deliveryFeeTotal);
        BigDecimal averageTicket = calculateAverageTicket(grossRevenue, deliveredOrders);
        BigDecimal cancellationRatePercent = calculateCancellationRate(canceledOrders, totalOrders);

        return new SalesSummary(
                totalOrders,
                deliveredOrders,
                canceledOrders,
                cancellationRatePercent,
                grossRevenue,
                deliveryFeeTotal,
                netProductRevenue,
                averageTicket
        );
    }

    public List<ProductPerformance> calculateProductRanking(List<ProductMetric> productMetrics) {
        BigDecimal totalProductRevenue = sumTotalProductRevenue(productMetrics);

        return productMetrics.stream()
                .map(metric -> buildProductPerformance(metric, totalProductRevenue))
                .toList();
    }

    private long sumTotalOrders(@NonNull List<StatusMetric> statusMetrics) {
        return statusMetrics.stream()
                .mapToLong(StatusMetric::orderCount)
                .sum();
    }

    private @NonNull Optional<StatusMetric> findByStatus(@NonNull List<StatusMetric> statusMetrics, String targetStatus) {
        return statusMetrics.stream()
                .filter(metric -> targetStatus.equals(metric.status()))
                .findFirst();
    }

    private BigDecimal calculateNetProductRevenue(@NonNull BigDecimal grossRevenue, BigDecimal deliveryFeeTotal) {
        return grossRevenue.subtract(deliveryFeeTotal);
    }

    private BigDecimal calculateAverageTicket(BigDecimal grossRevenue, long deliveredOrders) {
        if (deliveredOrders <= 0) {
            return BigDecimal.ZERO;
        }
        return grossRevenue.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCancellationRate(long canceledOrders, long totalOrders) {
        if (totalOrders <= 0 || canceledOrders <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(canceledOrders)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumTotalProductRevenue(@NonNull List<ProductMetric> productMetrics) {
        return productMetrics.stream()
                .map(ProductMetric::revenueGenerated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private @NonNull ProductPerformance buildProductPerformance(@NonNull ProductMetric metric, BigDecimal totalProductRevenue) {
        BigDecimal sharePercent = calculateRevenueShare(metric.revenueGenerated(), totalProductRevenue);
        return new ProductPerformance(
                metric.productId(),
                metric.productName(),
                metric.quantitySold(),
                metric.revenueGenerated(),
                sharePercent
        );
    }

    private BigDecimal calculateRevenueShare(BigDecimal productRevenue, @NonNull BigDecimal totalProductRevenue) {
        if (totalProductRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return productRevenue
                .multiply(ONE_HUNDRED)
                .divide(totalProductRevenue, 2, RoundingMode.HALF_UP);
    }
}
