package bti.pds.dinner.insights.domain;

import java.util.List;

public interface StockSnapshot {
    List<StockItemSnapshot> findActiveItems();
    List<StockLotSnapshot> findOpenLots();
}
