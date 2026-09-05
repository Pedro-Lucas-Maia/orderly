package bti.pds.dinner.product.domain;

import java.util.List;
import java.util.Optional;

public interface ProductCompositionRepository {
    ProductComposition save(ProductComposition productComposition);
    Optional<ProductComposition> findById(ProductCompositionId id);
    List<ProductComposition> findByProductId(ProductId productId);
    void delete(ProductComposition productComposition);
}
