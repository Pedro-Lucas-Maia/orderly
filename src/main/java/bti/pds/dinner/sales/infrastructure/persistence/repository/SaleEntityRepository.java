// src/main/java/bti/pds/dinner/sales/infrastructure/persistence/repository/SaleRepositoryImpl.java
package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleID;
import bti.pds.dinner.sales.domain.SaleItem;
import bti.pds.dinner.sales.domain.SaleRepository;
import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;
import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleItemEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class SaleEntityRepository implements SaleRepository {

    private final JpaSaleRepository jpaRepository;

    public SaleEntityRepository(JpaSaleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Sale sale) {

        SaleEntity entity = new SaleEntity(
                sale.getId().uuid().toString(),
                sale.getDate(),
                sale.getStatus(),
                sale.calculateTotal(),
                sale.getObservation());

        for (SaleItem item : sale.getItems()) {
            SaleItemEntity itemEntity = new SaleItemEntity(
                    item.getId(),
                    entity,
                    Long.parseLong(item.getProductId()),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal());

            entity.addItem(itemEntity);
        }

        jpaRepository.save(entity);
    }

    @Override
    public Optional<Sale> findById(SaleID id) {
        Optional<SaleEntity> entityOpt = jpaRepository.findById(id.uuid().toString());

        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        SaleEntity entity = entityOpt.get();

        Sale sale = new Sale(
                new SaleID(java.util.UUID.fromString(entity.getId())),
                entity.getDate(),
                entity.getStatus(),
                new java.util.ArrayList<>(),
                entity.getObservation());

        for (SaleItemEntity itemEntity : entity.getItens()) {
            sale.getItems().add(new SaleItem(
                    itemEntity.getId(),
                    itemEntity.getProductId().toString(),
                    itemEntity.getQuantity(),
                    itemEntity.getUnitPrice()));
        }

        return Optional.of(sale);
    }

    @Override
    public List<Sale> findAll() {
        List<SaleEntity> entities = jpaRepository.findAll();

        return entities.stream().map(entity -> {
            Sale sale = new Sale(
                    new SaleID(java.util.UUID.fromString(entity.getId())),
                    entity.getDate(),
                    entity.getStatus(),
                    new java.util.ArrayList<>(),
                    entity.getObservation());

            for (SaleItemEntity itemEntity : entity.getItens()) {
                sale.getItems().add(new SaleItem(
                        itemEntity.getId(),
                        itemEntity.getProductId().toString(),
                        itemEntity.getQuantity(),
                        itemEntity.getUnitPrice()));
            }

            return sale;
        }).toList();
    }
}
