package bti.pds.dinner.insights.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CriticalStockCalculator {
    private CriticalStockCalculator() {
    }

    public static List<CriticalStock> find(List<StockItemSnapshot> items, List<RecipeLine> recipes) {
        Map<Long, List<Long>> productsByItem = recipes.stream()
                .collect(Collectors.groupingBy(
                        RecipeLine::stockItemId,
                        Collectors.mapping(RecipeLine::productId, Collectors.toList())
                ));

        List<CriticalStock> critical = new ArrayList<>();
        for (StockItemSnapshot item : items) {
            if (item.currentQuantity() <= item.minimumStock()) {
                critical.add(new CriticalStock(
                        item.id(),
                        item.name(),
                        item.unit(),
                        item.currentQuantity(),
                        item.minimumStock(),
                        List.copyOf(productsByItem.getOrDefault(item.id(), List.of()))
                ));
            }
        }
        return critical;
    }

    public record CriticalStock(
            Long stockItemId,
            String name,
            String unit,
            int currentQuantity,
            int minimumStock,
            List<Long> affectedProductIds
    ) {
    }
}
