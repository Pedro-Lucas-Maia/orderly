package bti.pds.dinner.shoppingCart.domain;

import bti.pds.dinner.shoppingCart.domain.exceptions.InvalidInputException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

@Getter
@Builder
@EqualsAndHashCode
public class CartItem {
    private String productId;
    private String name;
    private int quantity;
    private BigDecimal unitPrice;
    private String observation;

    public CartItem(String productId, String name, int quantity, BigDecimal unitPrice, String observation) {
        var errorString = validateName(name) + validateQuantity(quantity) + validatePrice(unitPrice);
        if(errorString.isBlank()) {
            this.productId = productId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.observation = observation;
        } else {
            throw new InvalidInputException(errorString);
        }
    }

    private String validateName(@NonNull String name) {
        if (name.isBlank()) {
            return "Name is required! ";
        }
        return "";
    }
    private String validateQuantity(int quantity) {
        if (quantity < 0 ) {
            return "Quantity must be positive! ";
        }
        return "";
    }

    private String validatePrice(@NonNull BigDecimal unitPrice) {
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            return "Price must be positive! ";
        }
        return "";
    }

    public BigDecimal getTotalPrice() {
        return getUnitPrice().multiply(new BigDecimal(getQuantity()));
    }

    public CartItem withQuantity(int newQuantity) {
        return CartItem.builder()
                .productId(this.productId)
                .name(this.name)
                .quantity(newQuantity)
                .unitPrice(this.unitPrice)
                .observation(this.observation)
                .build();
    }
}

