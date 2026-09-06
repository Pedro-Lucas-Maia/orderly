package bti.pds.dinner.sales.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Sale {
    private SaleID id;
    private LocalDateTime date;
    private SaleStatus status;
    private List<SaleItem> items;
    private String observation;

    public Sale(SaleID id, String observation) {
        if (id == null) {
            throw new IllegalArgumentException("Sale ID cannot be null.");
        }
        this.id = id;
        this.date = LocalDateTime.now();
        this.status = SaleStatus.PENDING;
        this.items = new ArrayList<>();
        this.observation = observation;
    }

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

    public void cancel() {
        if (this.status == SaleStatus.CANCELLED) {
            throw new IllegalStateException("Esta venda já está cancelada.");
        }
        this.status = SaleStatus.CANCELLED;
    }
}
