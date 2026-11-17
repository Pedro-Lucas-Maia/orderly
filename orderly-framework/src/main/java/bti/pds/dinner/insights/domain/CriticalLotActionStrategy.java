package bti.pds.dinner.insights.domain;

import java.time.LocalDate;
import java.util.List;

public interface CriticalLotActionStrategy {

    List<InventoryAction> evaluateExpiringLots(
            List<StockLotSnapshot> lots,
            List<StockItemSnapshot> items,
            List<RecipeLine> recipes,
            List<ProductDemand> demand,
            LocalDate targetDate
    );
}
