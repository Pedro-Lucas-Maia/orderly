package bti.pds.dinner.stock.domain;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Stock save(Stock stock);
    Optional<Stock> findById(StockId id);
    List<Stock> findAll();
    void delete(Stock stock);
}
