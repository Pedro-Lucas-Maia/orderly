package bti.pds.dinner.inventory.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    List<Product> findAll();
    void delete(Product product);   
}
