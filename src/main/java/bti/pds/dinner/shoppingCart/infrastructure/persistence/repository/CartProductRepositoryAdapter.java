package bti.pds.dinner.shoppingCart.infrastructure.persistence.repository;

import bti.pds.dinner.product.application.service.ProductService;
import bti.pds.dinner.shoppingCart.domain.CartItem;
import bti.pds.dinner.shoppingCart.domain.ProductRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CartProductRepositoryAdapter implements ProductRepository {
    private final ProductService productService;

    public CartProductRepositoryAdapter(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public CartItem findById(String id) {
        var product = productService.findActiveProduct(CartProductRepositoryAdapter.from(id));
        return CartItem.builder()
                .productId(product.getId().value().toString())
                .name(product.getName())
                .unitPrice(product.getPrice())
                .build();
    }

    private static Long from(String id) {
        return Long.parseLong(id);
    }
}
