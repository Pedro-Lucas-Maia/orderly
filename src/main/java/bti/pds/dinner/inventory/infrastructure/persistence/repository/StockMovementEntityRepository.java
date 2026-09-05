package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import bti.pds.dinner.inventory.infrastructure.persistence.entity.StockMovementEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementEntityRepository extends CrudRepository<StockMovementEntity, Long> {
    @NonNull List<StockMovementEntity> findAll();

    List<StockMovementEntity> findByStockItemId(Long stockItemId);
}
