package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.common.domain.PageQuery;
import bti.pds.dinner.common.domain.PagedResult;
import bti.pds.dinner.stock.domain.*;
import bti.pds.dinner.stock.infrastructure.persistence.entity.StockItemEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static bti.pds.dinner.stock.infrastructure.persistence.repository.StockItemEntitySpecs.*;

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
    public Optional<StockItem> findByIdForUpdate(@NonNull StockItemId id) {
        return repository.findByIdForUpdate(id.value())
                .map(StockItemEntity::toDomain);
    }

    @Override
    public PagedResult<StockItem> findByStockId(@NonNull StockId stockId, StockItemFilter filter, PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
        var spec = belongsToStock(stockId.value())
                .and(isNotDeleted())
                .and(hasActive(filter.active()))
                .and(hasName(filter.name()))
                .and(hasCategory(filter.category()));
        Page<StockItemEntity> entityPage = repository.findBy(spec, q -> q.page(pageable));
        List<StockItem> items = entityPage.getContent().stream()
                .map(StockItemEntity::toDomain)
                .toList();

        return new PagedResult<>(
                items,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isFirst(),
                entityPage.isLast()
        );
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
