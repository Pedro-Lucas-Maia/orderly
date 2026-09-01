package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import bti.pds.dinner.inventory.domain.Inventory;
import bti.pds.dinner.inventory.domain.InventoryId;
import bti.pds.dinner.inventory.domain.InventoryItem;
import bti.pds.dinner.inventory.domain.InventoryRepository;
import bti.pds.dinner.inventory.domain.Product;
import bti.pds.dinner.inventory.domain.ProductId;
import bti.pds.dinner.inventory.infrastructure.persistence.entity.InventoryEntity;
import bti.pds.dinner.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import bti.pds.dinner.inventory.infrastructure.persistence.entity.ProductEntity;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = toEntity(inventory);

        InventoryEntity savedEntity = inventoryJpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    private InventoryEntity toEntity(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();

        entity.setId(inventory.getId().uuid());

        List<InventoryItemEntity> items = inventory.getItems()
                .stream()
                .map(item -> toItemEntity(item, entity))
                .toList();

        entity.setItems(items);

        return entity;
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {

        return inventoryJpaRepository
                .findById(id.uuid())
                .map(this::toDomain);
    }

    @Override
    public List<Inventory> findAll() {

        return inventoryJpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(Inventory inventory) {

        inventoryJpaRepository.deleteById(
                inventory.getId().uuid());
    }

    private InventoryItemEntity toItemEntity(
            InventoryItem item,
            InventoryEntity inventory) {

        InventoryItemEntity entity = new InventoryItemEntity();

        entity.setInventory(inventory);
        entity.setQuantity(item.getQuantity());

        ProductEntity product = new ProductEntity();

        product.setId(
                item.getProduct()
                        .getProductId()
                        .uuid());

        product.setName(
                item.getProduct().getName());

        product.setPrice(
                item.getProduct().getPrice());

        product.setDescription(
                item.getProduct().getDescription());

        entity.setProduct(product);

        return entity;
    }

    private Inventory toDomain(InventoryEntity entity) {

        List<InventoryItem> items = entity.getItems()
                .stream()
                .map(this::toItemDomain)
                .toList();

        return new Inventory(
                new InventoryId(entity.getId()),
                items);
    }

    private InventoryItem toItemDomain(
            InventoryItemEntity entity) {

        ProductEntity productEntity = entity.getProduct();

        Product product = new Product(
                new ProductId(productEntity.getId()),
                productEntity.getName(),
                productEntity.getPrice(),
                productEntity.getDescription());

        return new InventoryItem(
                product,
                entity.getQuantity());
    }
}
