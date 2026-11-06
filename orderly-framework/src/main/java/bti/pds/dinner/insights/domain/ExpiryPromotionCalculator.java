package bti.pds.dinner.insights.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ExpiryPromotionCalculator {
    private ExpiryPromotionCalculator() {
    }

    public static Result calculate(
            LocalDate targetDate,
            int expiryWindowDays,
            List<StockLotSnapshot> lots,
            List<StockItemSnapshot> items,
            List<RecipeLine> recipes,
            List<DemandForecastCalculator.ProductDemand> demand
    ) {
        LocalDate deadline = targetDate.plusDays(expiryWindowDays);
        Map<Long, StockItemSnapshot> itemsById = items.stream()
                .collect(Collectors.toMap(StockItemSnapshot::id, Function.identity()));
        Map<Long, DemandForecastCalculator.ProductDemand> demandByProduct = demand.stream()
                .collect(Collectors.toMap(DemandForecastCalculator.ProductDemand::productId, Function.identity()));
        Map<Long, List<Long>> productsByItem = recipes.stream()
                .collect(Collectors.groupingBy(
                        RecipeLine::stockItemId,
                        Collectors.mapping(RecipeLine::productId, Collectors.toList())
                ));

        List<ExpiringLot> expiring = new ArrayList<>();
        List<PromotionSuggestion> promotions = new ArrayList<>();

        lots.stream()
                .filter(lot -> lot.quantity() > 0)
                .filter(lot -> !lot.expiresAt().isAfter(deadline))
                .sorted(Comparator.comparing(StockLotSnapshot::expiresAt).thenComparing(StockLotSnapshot::lotId))
                .forEach(lot -> {
                    StockItemSnapshot item = itemsById.get(lot.stockItemId());
                    String itemName = item == null ? ("Item " + lot.stockItemId()) : item.name();
                    long days = ChronoUnit.DAYS.between(targetDate, lot.expiresAt());
                    expiring.add(new ExpiringLot(
                            lot.lotId(),
                            lot.stockItemId(),
                            itemName,
                            lot.quantity(),
                            lot.expiresAt(),
                            days
                    ));

                    List<SuggestedProduct> suggested = productsByItem.getOrDefault(lot.stockItemId(), List.of()).stream()
                            .map(demandByProduct::get)
                            .filter(Objects::nonNull)
                            .sorted(Comparator
                                    .comparingInt(DemandForecastCalculator.ProductDemand::predictedQuantity).reversed()
                                    .thenComparing(DemandForecastCalculator.ProductDemand::unitPrice, Comparator.reverseOrder())
                                    .thenComparing(DemandForecastCalculator.ProductDemand::productId))
                            .map(product -> new SuggestedProduct(
                                    product.productId(),
                                    product.productName(),
                                    product.predictedQuantity()
                            ))
                            .toList();
                    promotions.add(new PromotionSuggestion(
                            lot.stockItemId(),
                            lot.lotId(),
                            lot.expiresAt(),
                            lot.quantity(),
                            suggested
                    ));
                });

        return new Result(expiring, promotions);
    }

    public record Result(List<ExpiringLot> expiringLots, List<PromotionSuggestion> promotionSuggestions) {
    }

    public record ExpiringLot(
            Long lotId,
            Long stockItemId,
            String stockItemName,
            int quantity,
            LocalDate expiresAt,
            long daysUntilExpiry
    ) {
    }

    public record PromotionSuggestion(
            Long stockItemId,
            Long lotId,
            LocalDate expiresAt,
            int quantityInLot,
            List<SuggestedProduct> suggestedProducts
    ) {
    }

    public record SuggestedProduct(
            Long productId,
            String productName,
            int predictedQuantityToday
    ) {
    }
}
