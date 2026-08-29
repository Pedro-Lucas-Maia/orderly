package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import bti.pds.dinner.inventory.infrastructure.persistence.entity.ProductEntity;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    
}
