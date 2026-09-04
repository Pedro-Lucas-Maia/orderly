package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import bti.pds.dinner.inventory.infrastructure.persistence.entity.StockItemEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockItemEntityRepository extends CrudRepository<StockItemEntity, Long> {
    @NonNull List<StockItemEntity> findAll();

    List<StockItemEntity> findByStockId(Long stockId);
}
