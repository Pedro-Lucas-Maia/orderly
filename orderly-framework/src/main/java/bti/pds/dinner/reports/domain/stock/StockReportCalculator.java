package bti.pds.dinner.reports.domain.stock;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StockReportCalculator {

    private static final String TYPE_CONSUMPTION = "SAIDA";
    private static final String TYPE_LOSS = "PERDA";

    public InventoryValuation calculateValuation(List<StockItemSnapshot> activeItems) {
        int activeItemsCount = activeItems.size();
        int belowMinimumCount = countBelowMinimum(activeItems);
        int outOfStockCount = countOutOfStock(activeItems);
        BigDecimal totalInventoryValue = sumInventoryValue(activeItems);

        return new InventoryValuation(
                activeItemsCount,
                belowMinimumCount,
                outOfStockCount,
                totalInventoryValue
        );
    }

    public List<CategoryValuation> calculateValuationByCategory(List<StockItemSnapshot> activeItems) {
        Map<String, List<StockItemSnapshot>> itemsByCategory = activeItems.stream()
                .collect(Collectors.groupingBy(StockItemSnapshot::category));

        return itemsByCategory.entrySet().stream()
                .map(entry -> new CategoryValuation(
                        entry.getKey(),
                        entry.getValue().size(),
                        sumInventoryValue(entry.getValue())
                ))
                .sorted(Comparator.comparing(CategoryValuation::totalValue).reversed())
                .toList();
    }

    public List<ItemMovementHighlight> filterConsumedItems(List<ItemMovementMetric> itemMovements) {
        return filterByType(itemMovements, TYPE_CONSUMPTION);
    }

    public List<ItemMovementHighlight> filterLostItems(List<ItemMovementMetric> itemMovements) {
        return filterByType(itemMovements, TYPE_LOSS);
    }

    private int countBelowMinimum(List<StockItemSnapshot> items) {
        return (int) items.stream()
                .filter(item -> item.currentQuantity() < item.minimumStock())
                .count();
    }

    private int countOutOfStock(List<StockItemSnapshot> items) {
        return (int) items.stream()
                .filter(item -> item.currentQuantity() == 0)
                .count();
    }

    private BigDecimal sumInventoryValue(List<StockItemSnapshot> items) {
        return items.stream()
                .map(this::calculateItemTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateItemTotalValue(StockItemSnapshot item) {
        return item.unitCost().multiply(BigDecimal.valueOf(item.currentQuantity()));
    }

    private List<ItemMovementHighlight> filterByType(List<ItemMovementMetric> metrics, String targetType) {
        return metrics.stream()
                .filter(m -> targetType.equals(m.type()))
                .map(m -> new ItemMovementHighlight(
                        m.stockItemId(),
                        m.stockItemName(),
                        m.category(),
                        m.unit(),
                        m.totalQuantity(),
                        m.totalCost()
                ))
                .toList();
    }

}