package bti.pds.dinner.sales.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository {
    Sale save(Sale sale);
    Optional<Sale> findById(SaleId id);
    List<Sale> findAll(SaleStatus status, LocalDateTime date);
    List<Sale> findByUserId(UserId id);
}
