package bti.pds.dinner.product.domain;

import bti.pds.dinner.inventory.domain.StockItemId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductComposition {
    private ProductCompositionId id;
    private ProductId productId;
    private StockItemId stockItemId;
    private int quantity;

    public ProductComposition(ProductId productId, StockItemId stockItemId, int quantity) {
        this.id = null;
        this.productId = productId;
        this.stockItemId = stockItemId;
        this.quantity = quantity;
    }
}
