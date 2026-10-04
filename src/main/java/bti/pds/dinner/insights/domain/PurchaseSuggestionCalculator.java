package bti.pds.dinner.insights.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PurchaseSuggestionCalculator {
    private PurchaseSuggestionCalculator() {
    }

    public static List<PurchaseSuggestion> suggest(
            List<DemandForecastCalculator.ProductDemand> demand,
            List<RecipeLine> recipes,
            List<StockItemSnapshot> items,
            int horizonDays
    ) {
        Map<Long, Integer> predictedByProduct = demand.stream()
                .collect(Collectors.toMap(DemandForecastCalculator.ProductDemand::productId, DemandForecastCalculator.ProductDemand::predictedQuantity));

        Map<Long, Integer> consumption = new HashMap<>();
        Map<Long, List<Long>> affected = new HashMap<>();
        for (RecipeLine line : recipes) {
            int predicted = predictedByProduct.getOrDefault(line.productId(), 0);
            int used = predicted * line.quantityPerUnit() * horizonDays;
            consumption.merge(line.stockItemId(), used, Integer::sum);
            affected.computeIfAbsent(line.stockItemId(), key -> new ArrayList<>()).add(line.productId());
        }

        List<PurchaseSuggestion> suggestions = new ArrayList<>();
        for (StockItemSnapshot item : items) {
            int predictedConsumption = consumption.getOrDefault(item.id(), 0);
            int quantityToBuy = Math.max(0, predictedConsumption - item.currentQuantity());
            boolean belowMinimum = item.currentQuantity() <= item.minimumStock();
            suggestions.add(new PurchaseSuggestion(
                    item.id(),
                    item.name(),
                    item.unit(),
                    item.currentQuantity(),
                    item.minimumStock(),
                    predictedConsumption,
                    quantityToBuy,
                    belowMinimum,
                    List.copyOf(affected.getOrDefault(item.id(), List.of()))
            ));
        }

        suggestions.sort(Comparator
                .comparingInt(PurchaseSuggestion::quantityToBuy).reversed()
                .thenComparing(PurchaseSuggestion::belowMinimum, Comparator.reverseOrder()));
        return suggestions;
    }

    public record PurchaseSuggestion(
            Long stockItemId,
            String name,
            String unit,
            int currentQuantity,
            int minimumStock,
            int predictedConsumption,
            int quantityToBuy,
            boolean belowMinimum,
            List<Long> affectedProductIds
    ) {
    }
}
