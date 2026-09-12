package bti.pds.dinner.sales.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import bti.pds.dinner.sales.domain.exception.InvalidSaleItemException;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class SaleItem {
    
    private SaleItemId id;
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;

    public SaleItem(Long productId, int quantity, BigDecimal unitPrice) {
        this(new SaleItemId(), productId, quantity, unitPrice);
        validate(quantity, unitPrice);
    }

    private void validate(int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new InvalidSaleItemException("Quantity must be greater than zero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSaleItemException("Unit price cannot be null or negative.");
        }
    }

    public BigDecimal getSubtotal() {
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
