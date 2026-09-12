package bti.pds.dinner.sales.domain;

import java.util.List;
import java.util.Optional;

public interface SaleRepository {
    Sale save(Sale sale);
    Optional<Sale> findById(SaleId id);
    List<Sale> findAll();
}
