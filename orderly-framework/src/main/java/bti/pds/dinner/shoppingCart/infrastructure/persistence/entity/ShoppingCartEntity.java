package bti.pds.dinner.shoppingCart.infrastructure.persistence.entity;

import bti.pds.dinner.shoppingCart.domain.CartItem;
import bti.pds.dinner.shoppingCart.domain.ShoppingCart;
import bti.pds.dinner.shoppingCart.domain.ShoppingCartId;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "ShoppingCart", timeToLive = 3600)
public class ShoppingCartEntity {
    @Id
    private String id;

    private List<CartItem> items;

    public static ShoppingCart toDomain(@NonNull ShoppingCartEntity entity) {
        return ShoppingCart.builder()
                .id(new ShoppingCartId(entity.getId()))
                .items(entity.getItems() != null ? entity.getItems() : new java.util.ArrayList<>())
                .build();
    }

    public static ShoppingCartEntity from(@NonNull ShoppingCart cart) {
        return new ShoppingCartEntity(
                cart.getId().id(),
                cart.getItems()
        );
    }
}
