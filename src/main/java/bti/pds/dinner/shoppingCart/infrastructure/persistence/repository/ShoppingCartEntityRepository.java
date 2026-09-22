package bti.pds.dinner.shoppingCart.infrastructure.persistence.repository;

import bti.pds.dinner.shoppingCart.infrastructure.persistence.entity.ShoppingCartEntity;
import org.springframework.data.repository.CrudRepository;

public interface ShoppingCartEntityRepository extends CrudRepository<ShoppingCartEntity, String> {
}
