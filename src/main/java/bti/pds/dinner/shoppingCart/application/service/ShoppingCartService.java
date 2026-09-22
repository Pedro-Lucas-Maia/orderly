package bti.pds.dinner.shoppingCart.application.service;

import bti.pds.dinner.shoppingCart.application.input.AddItemInput;
import bti.pds.dinner.shoppingCart.application.input.DeleteItemInput;
import bti.pds.dinner.shoppingCart.application.input.UpdateItemQuantityInput;
import bti.pds.dinner.shoppingCart.application.output.CartOutput;
import bti.pds.dinner.shoppingCart.domain.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final ProductRepository productRepository;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository, ProductRepository productRepository) {
        this.cartRepository = shoppingCartRepository;
        this.productRepository = productRepository;
    }

    public CartOutput addItem(@NonNull AddItemInput input) {
        var product = productRepository.findById(input.productId());

        var newItem = new CartItem(
                product.getProductId(),
                product.getName(),
                input.quantity(),
                product.getUnitPrice(),
                input.observation()
        );
        var cart = findCart(input.id());

        cart.addItem(newItem);

        return CartOutput.from(cartRepository.save(cart));
    }

    public CartOutput getCart(String id) {
        return CartOutput.from(findCart(id));
    }

    public CartOutput deleteItem(@NonNull DeleteItemInput input) {
        var cart = findCart(input.id());
        cart.deleteItem(input.productId());
        return CartOutput.from(cartRepository.save(cart));
    }

    public CartOutput updateItemQuantity(@NonNull UpdateItemQuantityInput input) {
        var cart = findCart(input.id());
        cart.updateItemQuantity(input.productId(), input.quantity());
        return CartOutput.from(cartRepository.save(cart));
    }

    public CartOutput clearCart(String id) {
        var cart = findCart(id);

        if(cart.getItems().isEmpty()) return CartOutput.from(cart);

        return CartOutput.from(cartRepository.save(cart.clearCart()));
    }

    private ShoppingCart findCart(String id) {
        return cartRepository.findById(new ShoppingCartId(id))
                .orElseGet(() -> ShoppingCart.builder()
                        .id(new ShoppingCartId(id))
                        .items(new ArrayList<>())
                        .build()
                );
    }
}
