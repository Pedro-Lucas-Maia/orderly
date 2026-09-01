package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import bti.pds.dinner.inventory.infrastructure.persistence.entity.InventoryEntity;

public interface InventoryJpaRepository extends JpaRepository<InventoryEntity, UUID> {
    
}
