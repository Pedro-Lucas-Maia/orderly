package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.stock.infrastructure.persistence.entity.StockEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockEntityRepository extends ListCrudRepository<StockEntity, Long> {
}
