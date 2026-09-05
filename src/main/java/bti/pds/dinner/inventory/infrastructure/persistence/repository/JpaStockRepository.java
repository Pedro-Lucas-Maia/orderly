package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import bti.pds.dinner.inventory.domain.Stock;
import bti.pds.dinner.inventory.domain.StockId;
import bti.pds.dinner.inventory.domain.StockRepository;
import bti.pds.dinner.inventory.infrastructure.persistence.entity.StockEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaStockRepository implements StockRepository {
    private final StockEntityRepository repository;

    public JpaStockRepository(StockEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Stock save(Stock stock) {
        return StockEntity.toDomain(repository.save(StockEntity.from(stock)));
    }

    @Override
    public Optional<Stock> findById(@NonNull StockId id) {
        return repository.findById(id.value())
                .map(StockEntity::toDomain);
    }

    @Override
    public List<Stock> findAll() {
        return repository.findAll()
                .stream()
                .map(StockEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Stock stock) {
        repository.delete(StockEntity.from(stock));
    }
}
