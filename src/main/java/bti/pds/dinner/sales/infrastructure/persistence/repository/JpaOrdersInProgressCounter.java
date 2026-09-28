package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.SaleStatus;
import bti.pds.dinner.store.domain.OrdersInProgressCounter;
import org.springframework.stereotype.Repository;

import java.lang.Math;

@Repository
public class JpaOrdersInProgressCounter implements OrdersInProgressCounter {

    private final SaleEntityRepository saleRepository;

    public JpaOrdersInProgressCounter(SaleEntityRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Override
    public int countOrdersInProgress(Long storeId) {
        return Math.toIntExact(saleRepository.countByStoreIdAndStatus(storeId, SaleStatus.PENDING));
    }
}
