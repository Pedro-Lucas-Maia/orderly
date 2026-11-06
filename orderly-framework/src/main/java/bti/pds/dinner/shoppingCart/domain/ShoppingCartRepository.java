package bti.pds.dinner.shoppingCart.domain;

import java.util.Optional;

public interface ShoppingCartRepository {
    ShoppingCart save(ShoppingCart shoppingCart);
    Optional<ShoppingCart> findById(ShoppingCartId id);
    void deleteById(ShoppingCartId id);
}
