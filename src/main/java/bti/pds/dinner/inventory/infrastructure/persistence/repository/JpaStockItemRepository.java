package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import bti.pds.dinner.inventory.domain.StockId;
import bti.pds.dinner.inventory.domain.StockItem;
import bti.pds.dinner.inventory.domain.StockItemId;
import bti.pds.dinner.inventory.domain.StockItemRepository;
import bti.pds.dinner.inventory.infrastructure.persistence.entity.StockItemEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaStockItemRepository implements StockItemRepository {
    private final StockItemEntityRepository repository;

    public JpaStockItemRepository(StockItemEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public StockItem save(StockItem stockItem) {
        return StockItemEntity.toDomain(repository.save(StockItemEntity.from(stockItem)));
    }

    @Override
    public Optional<StockItem> findById(@NonNull StockItemId id) {
        return repository.findById(id.value())
                .map(StockItemEntity::toDomain);
    }

    @Override
    public List<StockItem> findByStockId(@NonNull StockId stockId) {
        return repository.findByStockId(stockId.value())
                .stream()
                .map(StockItemEntity::toDomain)
                .toList();
    }

    @Override
    public List<StockItem> findAll() {
        return repository.findAll()
                .stream()
                .map(StockItemEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(StockItem stockItem) {
        repository.delete(StockItemEntity.from(stockItem));
    }
}
