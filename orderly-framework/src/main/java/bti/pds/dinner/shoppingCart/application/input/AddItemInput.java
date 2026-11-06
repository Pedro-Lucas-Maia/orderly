package bti.pds.dinner.shoppingCart.application.input;

import java.math.BigDecimal;
import java.util.List;

public record AddItemInput(
    String id,
    String productId,
    int quantity,
    String observation
) {
}
