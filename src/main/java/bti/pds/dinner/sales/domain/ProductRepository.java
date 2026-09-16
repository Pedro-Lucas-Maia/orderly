package bti.pds.dinner.sales.domain;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository {
    BigDecimal getCurrentPrice(Long productId);
    List<RecipeItem> getRecipe(Long ProductId);
} 