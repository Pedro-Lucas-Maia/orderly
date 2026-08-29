package bti.pds.dinner.inventory.domain;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Inventory save(Inventory inventory);
    Optional<Inventory> findById(InventoryId id);
    List<Product> findAll();
    void delete(Inventory inventory);
}
