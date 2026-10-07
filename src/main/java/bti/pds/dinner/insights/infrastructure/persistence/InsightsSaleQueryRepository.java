package bti.pds.dinner.insights.infrastructure.persistence;

import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InsightsSaleQueryRepository extends Repository<SaleEntity, UUID> {

    @Query(value = """
            SELECT CAST(s.date AS date) AS sale_date,
                   si.product_id,
                   CAST(SUM(si.quantity) AS integer)
            FROM sales s
            INNER JOIN sale_items si ON si.sale_id = s.id
            WHERE s.status = 'ENTREGUE'
              AND s.store_id = :storeId
              AND s.date >= :fromInclusive
              AND s.date < :toExclusive
            GROUP BY CAST(s.date AS date), si.product_id
            """, nativeQuery = true)
    List<Object[]> aggregateByStore(
            @Param("storeId") Long storeId,
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive
    );

    @Query(value = """
            SELECT CAST(s.date AS date) AS sale_date,
                   si.product_id,
                   CAST(SUM(si.quantity) AS integer)
            FROM sales s
            INNER JOIN sale_items si ON si.sale_id = s.id
            WHERE s.status = 'ENTREGUE'
              AND s.date >= :fromInclusive
              AND s.date < :toExclusive
            GROUP BY CAST(s.date AS date), si.product_id
            """, nativeQuery = true)
    List<Object[]> aggregateAllStores(
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive
    );
}
