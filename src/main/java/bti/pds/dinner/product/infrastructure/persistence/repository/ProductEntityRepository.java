package bti.pds.dinner.product.infrastructure.persistence.repository;

import bti.pds.dinner.product.infrastructure.persistence.entity.ProductEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductEntityRepository extends CrudRepository<ProductEntity, Long> {
    @NonNull List<ProductEntity> findAll();
}
