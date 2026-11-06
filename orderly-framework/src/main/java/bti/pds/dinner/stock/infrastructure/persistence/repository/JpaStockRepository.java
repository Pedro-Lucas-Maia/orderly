package bti.pds.dinner.stock.infrastructure.persistence.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import bti.pds.dinner.stock.domain.Stock;
import bti.pds.dinner.stock.domain.StockId;
import bti.pds.dinner.stock.domain.StockRepository;
import bti.pds.dinner.stock.infrastructure.persistence.entity.StockEntity;

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
