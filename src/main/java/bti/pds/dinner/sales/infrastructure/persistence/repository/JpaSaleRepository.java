package bti.pds.dinner.sales.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;

public interface JpaSaleRepository extends JpaRepository<SaleEntity, String>{
    
}
