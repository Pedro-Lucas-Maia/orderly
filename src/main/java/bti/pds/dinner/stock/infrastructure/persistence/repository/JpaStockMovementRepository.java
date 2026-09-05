package bti.pds.dinner.stock.infrastructure.persistence.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockMovement;
import bti.pds.dinner.stock.domain.StockMovementId;
import bti.pds.dinner.stock.domain.StockMovementRepository;
import bti.pds.dinner.stock.infrastructure.persistence.entity.StockMovementEntity;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaStockMovementRepository implements StockMovementRepository {
    private final StockMovementEntityRepository repository;

    public JpaStockMovementRepository(StockMovementEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public StockMovement save(StockMovement stockMovement) {
        return StockMovementEntity.toDomain(repository.save(StockMovementEntity.from(stockMovement)));
    }

    @Override
    public Optional<StockMovement> findById(@NonNull StockMovementId id) {
        return repository.findById(id.value())
                .map(StockMovementEntity::toDomain);
    }

    @Override
    public List<StockMovement> findByStockItemId(@NonNull StockItemId stockItemId) {
        return repository.findByStockItemId(stockItemId.value())
                .stream()
                .map(StockMovementEntity::toDomain)
                .toList();
    }

    @Override
    public List<StockMovement> findAll() {
        return repository.findAll()
                .stream()
                .map(StockMovementEntity::toDomain)
                .toList();
    }
}
