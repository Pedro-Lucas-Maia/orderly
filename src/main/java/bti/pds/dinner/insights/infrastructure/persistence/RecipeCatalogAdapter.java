package bti.pds.dinner.insights.infrastructure.persistence;

import bti.pds.dinner.insights.domain.RecipeCatalog;
import bti.pds.dinner.insights.domain.RecipeLine;
import bti.pds.dinner.product.infrastructure.persistence.entity.ProductCompositionEntity;
import bti.pds.dinner.product.infrastructure.persistence.repository.ProductCompositionEntityRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecipeCatalogAdapter implements RecipeCatalog {
    private final ProductCompositionEntityRepository compositionRepository;

    public RecipeCatalogAdapter(ProductCompositionEntityRepository compositionRepository) {
        this.compositionRepository = compositionRepository;
    }

    @Override
    public List<RecipeLine> findAll() {
        List<ProductCompositionEntity> entities = new ArrayList<>();
        compositionRepository.findAll().forEach(entities::add);
        return entities.stream()
                .map(entity -> new RecipeLine(entity.getProductId(), entity.getStockItemId(), entity.getQuantity()))
                .toList();
    }
}
