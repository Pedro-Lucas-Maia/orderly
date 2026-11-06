package bti.pds.dinner.shoppingCart.domain;

public interface ProductRepository {
    CartItem findById(String id);
}
