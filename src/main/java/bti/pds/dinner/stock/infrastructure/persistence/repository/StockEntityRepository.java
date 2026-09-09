package bti.pds.dinner.stock.infrastructure.persistence.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import bti.pds.dinner.stock.infrastructure.persistence.entity.StockEntity;

import java.util.List;

@Repository
public interface StockEntityRepository extends CrudRepository<StockEntity, Long> {
    @NonNull List<StockEntity> findAll();
}
