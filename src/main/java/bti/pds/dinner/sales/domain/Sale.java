package bti.pds.dinner.sales.domain;

import bti.pds.dinner.sales.domain.exception.InvalidSaleStateException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Getter
@Builder
public class Sale {
    private SaleId id;
    private Long storeId;
    private LocalDateTime date;
    private SaleStatus status;
    private List<SaleItem> items;
    private String observation;
    private UserId userId;
    private BigDecimal deliveryFee;
    private String deliveryStreet;
    private String deliveryNumber;
    private String deliveryCity;
    private String deliveryNeighborhood;
    private String deliveryZipCode;

    public Sale(Long storeId, String observation, UserId userId, BigDecimal deliveryFee, String deliveryStreet, String deliveryNumber, String deliveryCity, String deliveryNeighborhood, String deliveryZipCode) {
        this.id = new SaleId();
        this.storeId = Objects.requireNonNull(storeId, "Store is required");
        this.date = LocalDateTime.now();
        this.status = SaleStatus.PENDING;
        this.items = new ArrayList<>();
        this.observation = observation;
        this.userId = userId;
        this.deliveryFee = deliveryFee;
        this.deliveryStreet = deliveryStreet;
        this.deliveryNumber = deliveryNumber;
        this.deliveryCity = deliveryCity;
        this.deliveryNeighborhood = deliveryNeighborhood;
        this.deliveryZipCode = deliveryZipCode;
    }

    public void addItem(Long productId, int quantity, BigDecimal unitPrice) {
        if (this.status != SaleStatus.PENDING) {
            throw new InvalidSaleStateException("Items can only be added to pending sales.");
        }
        this.items.add(new SaleItem(productId, quantity, unitPrice));
    }

    public BigDecimal calculateTotal(BigDecimal deliveryFee) {
        var subTotal =  items.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (deliveryFee == null) {
            return subTotal;
        }
        return deliveryFee.add(subTotal);
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
