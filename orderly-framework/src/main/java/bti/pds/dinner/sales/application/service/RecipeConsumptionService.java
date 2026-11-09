package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.domain.ProductRepository;
import bti.pds.dinner.sales.domain.RecipeItem;
import bti.pds.dinner.sales.domain.SaleConsumptionStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RecipeConsumptionService {

    private final ProductRepository productRepository;
    private final SaleConsumptionStrategy saleConsumptionStrategy;

    public RecipeConsumptionService(ProductRepository productRepository, SaleConsumptionStrategy saleConsumptionStrategy) {
        this.productRepository = productRepository;
        this.saleConsumptionStrategy = saleConsumptionStrategy;
    }

    public void accumulateConsumption(Map<Long, Integer> totalConsumption, Long productId, int quantity) {
        List<RecipeItem> recipe = productRepository.getRecipe(productId);
        double lossFactor = saleConsumptionStrategy.getProductionLossFactor();
        for (RecipeItem ingredient : recipe) {
            int baseQuantity = ingredient.quantityPerUnit() * quantity;
            int consumedQuantity = (int) Math.ceil(baseQuantity * (1.0 + lossFactor));
            totalConsumption.merge(ingredient.stockItemId(), consumedQuantity, Integer::sum);
        }
    }
}
