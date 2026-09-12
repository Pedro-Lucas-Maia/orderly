package bti.pds.dinner.sales.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import bti.pds.dinner.sales.domain.exception.InvalidSaleStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class Sale {
    private SaleId id;
    private LocalDateTime date;
    private SaleStatus status;
    private List<SaleItem> items;
    private String observation;

    public Sale(String observation) {
        this.id = new SaleId();
        this.date = LocalDateTime.now();
        this.status = SaleStatus.PENDING;
        this.items = new ArrayList<>();
        this.observation = observation;
    }

    public void addItem(Long productId, int quantity, BigDecimal unitPrice) {
        if (this.status != SaleStatus.PENDING) {
            throw new InvalidSaleStateException("Items can only be added to pending sales.");
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
            throw new InvalidSaleStateException("Only pending sales can be confirmed.");
        }
        this.status = SaleStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == SaleStatus.CANCELLED) {
            throw new InvalidSaleStateException("This sale is already cancelled.");
        }
        this.status = SaleStatus.CANCELLED;
    }
}
