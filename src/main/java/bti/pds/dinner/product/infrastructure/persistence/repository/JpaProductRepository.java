package bti.pds.dinner.product.infrastructure.persistence.repository;

import bti.pds.dinner.product.domain.Product;
import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.product.domain.ProductRepository;
import bti.pds.dinner.product.infrastructure.persistence.entity.ProductEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaProductRepository implements ProductRepository {
    private final ProductEntityRepository repository;

    public JpaProductRepository(ProductEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        return ProductEntity.toDomain(repository.save(ProductEntity.from(product)));
    }

    @Override
    public Optional<Product> findById(@NonNull ProductId id) {
        return repository.findById(id.value())
                .map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findAll(Boolean active) {
        List<ProductEntity> entities = active == null
                ? repository.findByDeletedAtIsNull()
                : repository.findByDeletedAtIsNullAndActive(active);

        return entities.stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Product product) {
        repository.delete(ProductEntity.from(product));
    }
}
