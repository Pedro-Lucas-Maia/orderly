package bti.pds.dinner.sales.domain;

import java.util.List;
import java.util.Optional;

public interface SaleRepository {
    void save(Sale sale);
    Optional<Sale> findById(SaleID id);
    List<Sale> findAll();
}
