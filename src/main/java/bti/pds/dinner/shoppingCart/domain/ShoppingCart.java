package bti.pds.dinner.shoppingCart.domain;

import bti.pds.dinner.shoppingCart.domain.exceptions.EmptyCartException;
import bti.pds.dinner.shoppingCart.domain.exceptions.ItemNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ShoppingCart {
    private ShoppingCartId id;
    private List<CartItem> items;

    public BigDecimal getSubTotal() {
        return items
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ShoppingCart addItem(CartItem cartItem) {
        this.items.add(cartItem);
        return this;
    }

    public ShoppingCart deleteItem(String productId) {
        if (this.getItems().isEmpty()) {
            throw new EmptyCartException("Shopping cart is already Empty");
        }
        var item = findItemByProductId(productId);

        if (items.remove(item)) {
         return this;
        }
        throw new ItemNotFoundException("Item with the id " + item.getProductId() + " not on the list");
    }

    public ShoppingCart updateItemQuantity(String productId, int newQuantity) {
        var item = findItemByProductId(productId);

        if (newQuantity == 0) {
            items.remove(item);
            return this;
        }

        int index = items.indexOf(item);
        items.set(index, item.withQuantity(newQuantity));
        return this;
    }

    private @NonNull CartItem findItemByProductId(String productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Item with the id " + productId + " not on the list"));
    }

    public ShoppingCart clearCart() {
        this.items = new ArrayList<>();
        return this;
    }
}
