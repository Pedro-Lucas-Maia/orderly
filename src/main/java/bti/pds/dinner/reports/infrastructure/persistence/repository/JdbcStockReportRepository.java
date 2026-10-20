package bti.pds.dinner.reports.infrastructure.persistence.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcStockReportRepository {
    private final JdbcClient jdbcClient;

    public JdbcStockReportRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public record StockItemSnapshotRow(
            Long id,
            String name,
            String category,
            String unit,
            int currentQuantity,
            int minimumStock,
            BigDecimal unitCost
    ) {}

    public record MovementTypeAggRow(
            String type,
            long movementsCount,
            int totalQuantity,
            BigDecimal estimatedFinancialValue
    ) {}

    public record ItemMovementAggRow(
            Long stockItemId,
            String stockItemName,
            String category,
            String unit,
            String type,
            int totalQuantity,
            BigDecimal totalCost
    ) {}

    // Snapshot atual dos insumos ativos
    public List<StockItemSnapshotRow> findActiveStockItems() {
        return jdbcClient.sql("""
                SELECT id,
                       name,
                       category,
                       unit,
                       current_quantity,
                       minimum_stock,
                       unit_cost
                FROM stock_items
                WHERE active = true
                    AND deleted_at IS NULL
                """)
                .query(StockItemSnapshotRow.class)
                .list();
    }

    //Balanço de movimentações agrupado por Tipo(ENTRADA, SAIDA, PERDA, AJUSTE)
    public List<MovementTypeAggRow> aggregateMovementsByType(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcClient.sql("""
                SELECT sm.type AS type,
                       COUNT(sm.id) AS movements_count,
                       CAST(COALESCE(SUM(sm.quantity), 0) AS integer) AS total_quantity,
                       COALESCE(SUM(sm.quantity * si.unit_cost), 0) AS estimated_financial_value
                FROM stock_movements sm
                INNER JOIN stock_items si ON si.id = sm.stock_item_id
                WHERE sm.occurred_at >= :fromInclusive
                    AND sm.occurred_at < :toExclusive
                GROUP BY sm.type
                """)
                .param("fromInclusive", fromInclusive)
                .param("toExclusive", toExclusive)
                .query(MovementTypeAggRow.class)
                .list();
    }

    //Movimentações agrupadas por Insumo e Tipo (para extrair top Consumo e top Perdas)
    public List<ItemMovementAggRow> aggregateMovementByItem(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return jdbcClient.sql("""
               SELECT si.id AS stock_item_id,
                   si.name AS stock_item_name,
                   si.category AS category,
                   si.unit AS unit,
                   sm.type AS type,
                   CAST(SUM(sm.quantity) AS integer) AS total_quantity,
                   COALESCE(SUM(sm.quantity * si.unit_cost), 0) AS total_cost
               FROM stock_movements sm
               INNER JOIN stock_items si ON si.id = sm.stock_item_id
               WHERE sm.occurred_at >= :fromInclusive
                   AND sm.occurred_at < :toExclusive
               GROUP BY si.id, si.name, si.category, si.unit, sm.type
               ORDER BY total_cost DESC
               """)
                .param("fromInclusive", fromInclusive)
                .param("toExclusive", toExclusive)
                .query(ItemMovementAggRow.class)
                .list();
    }
}
