package bti.pds.dinner.insights.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BurgerCriticalLotActionStrategy implements CriticalLotActionStrategy {

    private static final int EXPIRY_WINDOW_DAYS = 3;

    @Override
    public List<InventoryAction> evaluateExpiringLots(
            List<StockLotSnapshot> lots,
            List<StockItemSnapshot> items,
            List<RecipeLine> recipes,
            List<ProductDemand> demand,
            LocalDate targetDate
    ) {
        LocalDate deadline = targetDate.plusDays(EXPIRY_WINDOW_DAYS);
        Map<Long, ProductDemand> demandByProduct = demand.stream()
                .collect(Collectors.toMap(ProductDemand::productId, Function.identity()));
        Map<Long, List<Long>> productsByItem = recipes.stream()
                .collect(Collectors.groupingBy(
                        RecipeLine::stockItemId,
                        Collectors.mapping(RecipeLine::productId, Collectors.toList())
                ));

        List<InventoryAction> actions = new ArrayList<>();

        lots.stream()
                .filter(lot -> lot.quantity() > 0)
                .filter(lot -> !lot.expiresAt().isAfter(deadline))
                .sorted(Comparator.comparing(StockLotSnapshot::expiresAt).thenComparing(StockLotSnapshot::lotId))
                .forEach(lot -> {
                    List<SuggestedProduct> suggested = productsByItem.getOrDefault(lot.stockItemId(), List.of()).stream()
                            .map(demandByProduct::get)
                            .filter(Objects::nonNull)
                            .sorted(Comparator
                                    .comparingInt(ProductDemand::predictedQuantity).reversed()
                                    .thenComparing(ProductDemand::unitPrice, Comparator.reverseOrder())
                                    .thenComparing(ProductDemand::productId))
                            .map(product -> new SuggestedProduct(
                                    product.productId(),
                                    product.productName(),
                                    product.predictedQuantity()
                            ))
                            .toList();

                    actions.add(new PromotionSuggestion(
                            lot.stockItemId(),
                            lot.lotId(),
                            lot.expiresAt(),
                            lot.quantity(),
                            suggested
                    ));
                });

        return actions;
    }
}
