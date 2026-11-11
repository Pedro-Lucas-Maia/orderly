package bti.pds.dinner.sales.domain;

import bti.pds.dinner.sales.domain.exception.InvalidSaleStateException;
import bti.pds.dinner.sales.domain.exception.InvalidSaleItemException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Getter
@Builder
public class Sale {
    private SaleId id;
    private LocalDateTime date;
    private SaleStatus status;
    private List<SaleItem> items;
    private String observation;
    private UserId userId;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String deliveryStreet;
    private String deliveryNumber;
    private String deliveryCity;
    private String deliveryNeighborhood;
    private String deliveryZipCode;

    public static Sale createNew(
            String observation,
            UserId userId,
            Address deliveryAddress,
            List<SaleItem> items,
            OrderPricingStrategy pricingStrategy
    ) {
        if (items == null || items.isEmpty()) {
            throw new InvalidSaleItemException("A sale must contain at least one item");
        }
        Objects.requireNonNull(pricingStrategy,  "Pricing strategy is required to create a sale");
        Sale sale = new SaleBuilder()
                .id(new SaleId()).date(LocalDateTime.now()).status(SaleStatus.PENDENTE)
                .items(new ArrayList<>(items)).observation(observation).userId(userId)
                .deliveryFee(null).totalAmount(null).deliveryStreet(deliveryAddress.getStreet())
                .deliveryNumber(deliveryAddress.getNumber()).deliveryCity(deliveryAddress.getCity())
                .deliveryNeighborhood(deliveryAddress.getNeighborhood()).deliveryZipCode(deliveryAddress.getZipCode())
                .build();

        sale.recalculatePricing(deliveryAddress, pricingStrategy);

        return sale;
    }

    private void recalculatePricing(Address address, @NonNull OrderPricingStrategy pricingStrategy) {
        this.deliveryFee = pricingStrategy.calculateDeliveryFee(address);
        this.totalAmount = pricingStrategy.calculateOrderTotal(this, Collections.unmodifiableList(this.items), this.deliveryFee);

        if (this.totalAmount == null || this.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSaleStateException("Calculated sale total must be a positive number");
        }
    }

    public static Sale reconstitute(
            SaleId id, LocalDateTime date, SaleStatus status, List<SaleItem> items,
            BigDecimal totalAmount, String observation, UserId userId, BigDecimal deliveryFee,
            String deliveryStreet,  String deliveryNumber, String deliveryCity,
            String deliveryNeighborhood, String deliveryZipCode
    ) {
        Objects.requireNonNull(totalAmount, "Persisted sale must have a total amount");
        return new SaleBuilder()
                .id(id).date(date).status(status).items(items).observation(observation)
                .userId(userId).deliveryFee(deliveryFee).totalAmount(totalAmount)
                .deliveryStreet(deliveryStreet).deliveryNumber(deliveryNumber).deliveryCity(deliveryCity)
                .deliveryNeighborhood(deliveryNeighborhood).deliveryZipCode(deliveryZipCode).build();
    }


    public void confirm() {
        if (this.status != SaleStatus.PENDENTE) {
            throw new InvalidSaleStateException("Only pending sales can be confirmed.");
        }
        this.status = SaleStatus.EM_PREPARO;
    }

    public void dispatch() {
        if (this.status != SaleStatus.EM_PREPARO) {
            throw new InvalidSaleStateException("Only preparing sales can be dispatched.");
        }
        this.status = SaleStatus.EM_ROTA;
    }

    public void deliver() {
        if (this.status != SaleStatus.EM_ROTA) {
            throw new InvalidSaleStateException("Only dispatched sales can be delivered.");
        }
        this.status = SaleStatus.ENTREGUE;
    }

    public void cancel(SaleConsumptionStrategy strategy) {
        if (this.status == SaleStatus.CANCELADA) {
            throw new InvalidSaleStateException("This sale is already cancelled.");
        }
        if (strategy == null) {
            throw new InvalidSaleStateException("Strategy is required to cancel a sale.");
        }
        if (strategy.canBeCancelled(this.status)) {
            this.status = SaleStatus.CANCELADA;
            return;
        }
        throw new InvalidSaleStateException("A sale that is " + this.status + " can not be cancelled.");
    }
}
