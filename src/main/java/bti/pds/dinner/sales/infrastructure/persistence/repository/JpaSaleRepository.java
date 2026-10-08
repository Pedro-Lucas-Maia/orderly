// src/main/java/bti/pds/dinner/sales/infrastructure/persistence/repository/SaleRepositoryImpl.java
package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.*;
import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static bti.pds.dinner.sales.infrastructure.persistence.repository.SaleEntitySpecs.hasDate;
import static bti.pds.dinner.sales.infrastructure.persistence.repository.SaleEntitySpecs.hasStatus;

@Repository
public class JpaSaleRepository implements SaleRepository {

    private final SaleEntityRepository jpaRepository;

    public JpaSaleRepository(SaleEntityRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Sale save(Sale sale) {
        return SaleEntity.toDomain(jpaRepository.save(SaleEntity.from(sale)));
    }

    @Override
    public Optional<Sale> findById(@NonNull SaleId id) {
        return jpaRepository.findById(id.uuid())
                .map(SaleEntity::toDomain);
    }

    @Override
    public List<Sale> findAll(SaleStatus saleStatus, LocalDateTime date) {
        var spec = hasStatus(saleStatus).and(hasDate(date));
        return jpaRepository.findBy(spec, q -> q.all())
                .stream()
                .map(SaleEntity::toDomain)
                .toList();
    }

    @Override
    public List<Sale> findByUserId(@NonNull UserId userId) {
        return jpaRepository.findByUserId(userId.uuid())
                .stream()
                .map(SaleEntity::toDomain)
                .toList();
    }
}
