package bti.pds.dinner.stock.infrastructure.persistence.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import bti.pds.dinner.stock.infrastructure.persistence.entity.StockMovementEntity;

import java.util.List;

@Repository
public interface StockMovementEntityRepository extends CrudRepository<StockMovementEntity, Long> {
    @NonNull List<StockMovementEntity> findAll();

    List<StockMovementEntity> findByStockItemId(Long stockItemId);
}
