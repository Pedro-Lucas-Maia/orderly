// src/main/java/bti/pds/dinner/sales/infrastructure/persistence/repository/SaleRepositoryImpl.java
package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleID;
import bti.pds.dinner.sales.domain.SaleRepository;
import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SaleEntityRepository implements SaleRepository {

    private final JpaSaleRepository jpaRepository;

    public SaleEntityRepository(JpaSaleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Sale save(Sale sale) {
        return SaleEntity.toDomain(jpaRepository.save(SaleEntity.from(sale)));
    }

    @Override
    public Optional<Sale> findById(@NonNull SaleID id) {
        return jpaRepository.findById(id.uuid().toString())
                .map(SaleEntity::toDomain);
    }

    @Override
    public List<Sale> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(SaleEntity::toDomain)
                .toList();
    }
}
