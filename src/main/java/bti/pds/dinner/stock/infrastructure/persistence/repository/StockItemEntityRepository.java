package bti.pds.dinner.stock.infrastructure.persistence.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import bti.pds.dinner.stock.infrastructure.persistence.entity.StockItemEntity;

import java.util.List;

@Repository
public interface StockItemEntityRepository extends CrudRepository<StockItemEntity, Long> {
    @NonNull List<StockItemEntity> findAll();

    List<StockItemEntity> findByStockId(Long stockId);
}
