package bti.pds.dinner.sales.infrastructure.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;

public interface JpaSaleRepository extends CrudRepository<SaleEntity, String>{
    
}
