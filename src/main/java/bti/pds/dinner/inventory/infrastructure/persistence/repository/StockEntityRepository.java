package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import bti.pds.dinner.inventory.infrastructure.persistence.entity.StockEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockEntityRepository extends CrudRepository<StockEntity, Long> {
    @NonNull List<StockEntity> findAll();
}
