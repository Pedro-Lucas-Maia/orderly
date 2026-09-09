// domain/model/SaleItem.java
package bti.pds.dinner.sales.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SaleItem {
    
    private SaleItemId id;
    private String productId;
    private int quantity;
    private BigDecimal unitPrice;

    public SaleItem(String productId, int quantity, BigDecimal unitPrice) {
        this(new SaleItemId(), productId, quantity, unitPrice);
    }


    private void validate(int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be null or negative.");
        }
    }

    public BigDecimal getSubtotal() {
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
