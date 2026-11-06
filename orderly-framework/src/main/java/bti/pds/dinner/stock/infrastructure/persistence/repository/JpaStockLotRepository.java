package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockLot;
import bti.pds.dinner.stock.domain.StockLotId;
import bti.pds.dinner.stock.domain.StockLotRepository;
import bti.pds.dinner.stock.infrastructure.persistence.entity.StockLotEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaStockLotRepository implements StockLotRepository {
    private final StockLotEntityRepository repository;

    public JpaStockLotRepository(StockLotEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public StockLot save(StockLot stockLot) {
        return StockLotEntity.toDomain(repository.save(StockLotEntity.from(stockLot)));
    }

    @Override
    public Optional<StockLot> findById(@NonNull StockLotId id) {
        return repository.findById(id.value()).map(StockLotEntity::toDomain);
    }

    @Override
    public List<StockLot> findByStockItemIdForUpdate(@NonNull StockItemId stockItemId) {
        return repository.findByStockItemIdForUpdate(stockItemId.value()).stream()
                .map(StockLotEntity::toDomain)
                .toList();
    }

    @Override
    public List<StockLot> findByStockItemId(@NonNull StockItemId stockItemId) {
        return repository.findByStockItemIdOrderByExpiresAtAscIdAsc(stockItemId.value()).stream()
                .map(StockLotEntity::toDomain)
                .toList();
    }

    @Override
    public List<StockLot> findAll() {
        List<StockLotEntity> lots = new ArrayList<>();
        repository.findAll().forEach(lots::add);
        return lots.stream().map(StockLotEntity::toDomain).toList();
    }

    @Override
    public void delete(StockLot stockLot) {
        repository.delete(StockLotEntity.from(stockLot));
    }
}
