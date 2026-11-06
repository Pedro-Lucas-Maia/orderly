package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.stock.infrastructure.persistence.entity.StockLotEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockLotEntityRepository extends CrudRepository<StockLotEntity, Long> {
    List<StockLotEntity> findByStockItemIdOrderByExpiresAtAscIdAsc(Long stockItemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lot from StockLotEntity lot where lot.stockItemId = :stockItemId order by lot.expiresAt asc, lot.id asc")
    List<StockLotEntity> findByStockItemIdForUpdate(@Param("stockItemId") Long stockItemId);
}
