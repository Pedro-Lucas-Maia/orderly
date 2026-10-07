package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.domain.ProductRepository;
import bti.pds.dinner.sales.domain.RecipeItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RecipeConsumptionService {

    private final ProductRepository productRepository;

    public RecipeConsumptionService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void accumulateConsumption(Map<Long, Integer> totalConsumption, Long productId, int quantity) {
        List<RecipeItem> recipe = productRepository.getRecipe(productId);
        for (RecipeItem ingredient : recipe) {
            int consumedQuantity = ingredient.quantityPerUnit() * quantity;
            totalConsumption.merge(ingredient.stockItemId(), consumedQuantity, Integer::sum);
        }
    }
}
