package bti.pds.dinner.sales.domain;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository {
    BigDecimal getCurrentPrice(String productId);
    List<RecipeItem> getRecipe(String ProductId);
} 