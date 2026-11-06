package bti.pds.dinner.reports.infrastructure.persistence.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcSalesReportRepository {
    private final JdbcClient jdbcClient;

    public JdbcSalesReportRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public record StatusAggRow(
            String status,
            long orderCount,
            BigDecimal totalAmount,
            BigDecimal deliveryFeeAmount
    ) {}

    public record DailySalesRow(
            LocalDate saleDate,
            long deliveredOrders,
            BigDecimal dailyRevenue
    ) {}

    public record ProductSalesRow(
            Long productId,
            String productName,
            int quantitySold,
            BigDecimal revenueGenerated
    ) {}

    //Vendas agrupadas por status
    public List<StatusAggRow> aggregateByStatus(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcClient.sql("""
                SELECT s.status AS status,
                       COUNT (s.id) AS order_count,
                       COALESCE(SUM(s.total_value), 0) AS total_amount,
                       COALESCE(SUM(s.delivery_fee), 0) AS delivery_fee_amount
                FROM sales s
                WHERE s.date >= :fromInclusive
                    AND s.date < :toExclusive
                GROUP BY s.status
                """)
                .param("fromInclusive", fromInclusive)
                .param("toExclusive", toExclusive)
                .query(StatusAggRow.class)
                .list();
    }

    //Evolução diária (série temporal de vendas entregues)
    public List<DailySalesRow> aggregateDailyDelivered(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcClient.sql("""
               SELECT CAST(s.date AS date) AS sale_date,
                      COUNT(s.id) AS delivered_orders,
                      COALESCE(SUM(s.total_value), 0) AS daily_revenue
               FROM sales s
               WHERE s.status = 'ENTREGUE'
                   AND s.date >= :fromInclusive
                   AND s.date < :toExclusive
               GROUP BY CAST(s.date AS date)
               ORDER BY sale_date ASC
               """)
                .param("fromInclusive", fromInclusive)
                .param("toExclusive", toExclusive)
                .query(DailySalesRow.class)
                .list();
    }

    // Ranking de produtos vendidos
    public List<ProductSalesRow> aggregateTopProducts(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcClient.sql("""
                SELECT p.id AS product_id,
                       p.name AS product_name,
                       CAST(SUM(si.quantity) AS integer) AS quantity_sold,
                       COALESCE(SUM(si.subtotal), 0) AS revenue_generated
                FROM sales s
                INNER JOIN sale_items si ON si.sale_id = s.id
                INNER JOIN products p ON p.id = si.product_id
                WHERE s.status = 'ENTREGUE'
                    AND s.date >= :fromInclusive
                    AND s.date < :toExclusive
                GROUP BY p.id, p.name
                ORDER BY revenue_generated DESC, quantity_sold DESC
                """)
                .param("fromInclusive", fromInclusive)
                .param("toExclusive", toExclusive)
                .query(ProductSalesRow.class)
                .list();
    }
}
