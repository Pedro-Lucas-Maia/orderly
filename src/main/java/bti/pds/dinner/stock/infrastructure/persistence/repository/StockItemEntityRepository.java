package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.stock.infrastructure.persistence.entity.StockItemEntity;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemEntityRepository extends CrudRepository<StockItemEntity, Long>, JpaSpecificationExecutor<StockItemEntity> {
    @NonNull List<StockItemEntity> findAll();

    List<StockItemEntity> findByStockId(Long stockId);

    List<StockItemEntity> findByDeletedAtIsNull();

    List<StockItemEntity> findByDeletedAtIsNullAndActive(boolean active);

    List<StockItemEntity> findByStockIdAndDeletedAtIsNull(Long stockId);

    List<StockItemEntity> findByStockIdAndDeletedAtIsNullAndActive(Long stockId, boolean active);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from StockItemEntity item where item.id = :id")
    Optional<StockItemEntity> findByIdForUpdate(Long id);
}
