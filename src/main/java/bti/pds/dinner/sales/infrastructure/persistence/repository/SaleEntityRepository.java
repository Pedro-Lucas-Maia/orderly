package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.SaleStatus;
import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface SaleEntityRepository extends JpaRepository<SaleEntity, UUID>, JpaSpecificationExecutor<SaleEntity> {
    long countByStoreIdAndStatus(Long storeId, SaleStatus status);
    List<SaleEntity> findByUserId(UUID userId);
}
