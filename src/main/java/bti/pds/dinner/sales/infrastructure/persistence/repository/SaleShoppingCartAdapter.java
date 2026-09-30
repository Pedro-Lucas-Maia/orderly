package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.domain.SaleShoppingCartRepository;
import bti.pds.dinner.shoppingCart.application.service.ShoppingCartService;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SaleShoppingCartAdapter implements SaleShoppingCartRepository {
    private final ShoppingCartService service;

    public SaleShoppingCartAdapter(ShoppingCartService service) {
        this.service = service;
    }

    @Override
    public List<SaleItemInput> getItems(String shoppingCartId) {
        var output = service.getCart(shoppingCartId);

        return output.items()
                .stream()
                .map(cartItem -> new SaleItemInput(
                        Long.parseLong(cartItem.getProductId()),
                        cartItem.getQuantity()
                ))
                .toList();
    }

    @Override
    public void clearItems(String shoppingCartId) {
        service.clearCart(shoppingCartId);
    }
}
