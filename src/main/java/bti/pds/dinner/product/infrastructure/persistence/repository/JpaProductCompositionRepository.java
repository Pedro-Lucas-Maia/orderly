package bti.pds.dinner.product.infrastructure.persistence.repository;

import bti.pds.dinner.product.domain.ProductComposition;
import bti.pds.dinner.product.domain.ProductCompositionId;
import bti.pds.dinner.product.domain.ProductCompositionRepository;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.product.infrastructure.persistence.entity.ProductCompositionEntity;
import bti.pds.dinner.stock.domain.StockItemId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaProductCompositionRepository implements ProductCompositionRepository {
    private final ProductCompositionEntityRepository repository;

    public JpaProductCompositionRepository(ProductCompositionEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductComposition save(ProductComposition productComposition) {
        return ProductCompositionEntity.toDomain(repository.save(ProductCompositionEntity.from(productComposition)));
    }

    @Override
    public Optional<ProductComposition> findById(@NonNull ProductCompositionId id) {
        return repository.findById(id.value())
                .map(ProductCompositionEntity::toDomain);
    }

    @Override
    public List<ProductComposition> findByProductId(@NonNull ProductId productId) {
        return repository.findByProductId(productId.value())
                .stream()
                .map(ProductCompositionEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(ProductComposition productComposition) {
        repository.delete(ProductCompositionEntity.from(productComposition));
    }

    @Override
    public void deleteByProductId(@NonNull ProductId productId) {
        repository.deleteByProductId(productId.value());
    }

    @Override
    public void deleteByStockItemId(@NonNull StockItemId stockItemId) {
        repository.deleteByStockItemId(stockItemId.value());
    }
}
