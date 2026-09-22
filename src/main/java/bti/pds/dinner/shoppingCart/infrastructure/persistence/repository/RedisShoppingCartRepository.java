package bti.pds.dinner.shoppingCart.infrastructure.persistence.repository;

import bti.pds.dinner.shoppingCart.domain.ShoppingCart;
import bti.pds.dinner.shoppingCart.domain.ShoppingCartId;
import bti.pds.dinner.shoppingCart.domain.ShoppingCartRepository;
import bti.pds.dinner.shoppingCart.infrastructure.persistence.entity.ShoppingCartEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RedisShoppingCartRepository implements ShoppingCartRepository {
    private final ShoppingCartEntityRepository repository;

    public RedisShoppingCartRepository(ShoppingCartEntityRepository shoppingCartEntityRepository) {
        this.repository = shoppingCartEntityRepository;
    }

    @Override
    public ShoppingCart save(ShoppingCart shoppingCart) {
        return ShoppingCartEntity.toDomain(repository.save(ShoppingCartEntity.from(shoppingCart)));
    }

    @Override
    public Optional<ShoppingCart> findById(ShoppingCartId id) {
        return repository.findById(id.id())
                .map(ShoppingCartEntity::toDomain);
    }

    @Override
    public void deleteById(ShoppingCartId id) {
        repository.deleteById(id.id());
    }
}
