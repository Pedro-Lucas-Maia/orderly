package bti.pds.dinner.product.infrastructure.persistence.repository;

import bti.pds.dinner.product.infrastructure.persistence.entity.ProductCompositionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCompositionEntityRepository extends CrudRepository<ProductCompositionEntity, Long> {
    List<ProductCompositionEntity> findByProductId(Long productId);

    void deleteByProductId(Long productId);

    void deleteByStockItemId(Long stockItemId);
}
