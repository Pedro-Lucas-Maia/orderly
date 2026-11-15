package bti.pds.dinner.shoppingCart.application.service;

import bti.pds.dinner.shoppingCart.application.input.AddItemInput;
import bti.pds.dinner.shoppingCart.application.input.DeleteItemInput;
import bti.pds.dinner.shoppingCart.application.input.UpdateItemQuantityInput;
import bti.pds.dinner.shoppingCart.application.output.CartOutput;
import bti.pds.dinner.shoppingCart.domain.*;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final ProductRepository productRepository;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository, ProductRepository productRepository) {
        this.cartRepository = shoppingCartRepository;
        this.productRepository = productRepository;
    }

    @Observed(name = "shopping-cart.user.add")
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
        var savedCart = cartRepository.save(cart);
        log.info("item added to the cart. userId={}, productId={}, cartId={}", savedCart.getId(), product.getProductId(), savedCart.getId());
        return CartOutput.from(savedCart);
    }

    public CartOutput getCart(String id) {
        return CartOutput.from(findCart(id));
    }

    @Observed(name = "shopping-cart.user.delete-item")
    public CartOutput deleteItem(@NonNull DeleteItemInput input) {
        var cart = findCart(input.id());
        cart.deleteItem(input.productId());
        var savedCart = cartRepository.save(cart);
        log.info("Item removed. userId={}, productId={}, cartId={}", savedCart.getId(), input.productId(), savedCart.getId());
        return CartOutput.from(savedCart);
    }

    @Observed(name = "shopping-cart.user.update")
    public CartOutput updateItemQuantity(@NonNull UpdateItemQuantityInput input) {
        var cart = findCart(input.id());
        cart.updateItemQuantity(input.productId(), input.quantity());
        var savedCart = cartRepository.save(cart);
        log.info("Item updated. userId={}, productId={}, cartId={}", savedCart.getId(), input.productId(), savedCart.getId());
        return CartOutput.from(savedCart);
    }

    @Observed(name = "shopping-cart.user.clear")
    public CartOutput clearCart(String id) {
        var cart = findCart(id);

        if(cart.getItems().isEmpty()) return CartOutput.from(cart);

        var savedCart = cartRepository.save(cart.clearCart());
        log.info("Cart cleared. userId={}, cartId={}", savedCart.getId(), id);
        return CartOutput.from(savedCart);
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
