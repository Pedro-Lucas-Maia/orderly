package bti.pds.dinner.sales.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Sale {
    private SaleID id;
    private LocalDateTime date;
    private SaleStatus status;
    private List<SaleItem> items;
    private String observation;

    public void addItem(String productId, int quantity, BigDecimal unitPrice) {
        if (this.status != SaleStatus.PENDING) {
            throw new IllegalStateException("Items can only be added to pending sales.");
        }
        this.items.add(new SaleItem(productId, quantity, unitPrice));
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (this.status != SaleStatus.PENDING) {
            throw new IllegalStateException("Only pending sales can be confirmed.");
        }
        this.status = SaleStatus.CONFIRMED;
    }
}
