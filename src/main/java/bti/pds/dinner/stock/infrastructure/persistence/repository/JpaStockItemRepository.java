package bti.pds.dinner.stock.infrastructure.persistence.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import bti.pds.dinner.stock.domain.StockId;
import bti.pds.dinner.stock.domain.StockItem;
import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockItemRepository;
import bti.pds.dinner.stock.infrastructure.persistence.entity.StockItemEntity;

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
    public List<StockItem> findByStockId(@NonNull StockId stockId, Boolean active) {
        List<StockItemEntity> entities = active == null
                ? repository.findByStockIdAndDeletedAtIsNull(stockId.value())
                : repository.findByStockIdAndDeletedAtIsNullAndActive(stockId.value(), active);

        return entities.stream()
                .map(StockItemEntity::toDomain)
                .toList();
    }

    @Override
    public List<StockItem> findAll(Boolean active) {
        List<StockItemEntity> entities = active == null
                ? repository.findByDeletedAtIsNull()
                : repository.findByDeletedAtIsNullAndActive(active);

        return entities.stream()
                .map(StockItemEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(StockItem stockItem) {
        repository.delete(StockItemEntity.from(stockItem));
    }
}
