package bti.pds.dinner.insights.application.service;

import bti.pds.dinner.insights.application.output.DailyInsightsOutput;
import bti.pds.dinner.insights.domain.*;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DailyInsightsService {

    public static final String STOCK_SCOPE_GLOBAL = "GLOBAL_SHARED";
    public static final String DEMAND_SCOPE_STORE = "STORE";

    private final StoreCatalog storeCatalog;
    private final ConfirmedSalesHistory salesHistory;
    private final ProductCatalog productCatalog;
    private final RecipeCatalog recipeCatalog;
    private final StockSnapshot stockSnapshot;
    private final DemandForecastStrategy demandForecastStrategy;
    private final CriticalLotActionStrategy criticalLotActionStrategy;
    private final Clock clock;
    private final int forecastWeeks;
    private final int defaultExpiryWindowDays;
    private final int defaultHorizonDays;

    public DailyInsightsService(
            StoreCatalog storeCatalog,
            ConfirmedSalesHistory salesHistory,
            ProductCatalog productCatalog,
            RecipeCatalog recipeCatalog,
            StockSnapshot stockSnapshot,
            DemandForecastStrategy demandForecastStrategy,
            CriticalLotActionStrategy criticalLotActionStrategy,
            Clock clock,
            @Value("${app.insights.forecast-weeks:8}") int forecastWeeks,
            @Value("${app.insights.expiry-window-days:3}") int defaultExpiryWindowDays,
            @Value("${app.insights.horizon-days:1}") int defaultHorizonDays
    ) {
        this.storeCatalog = storeCatalog;
        this.salesHistory = salesHistory;
        this.productCatalog = productCatalog;
        this.recipeCatalog = recipeCatalog;
        this.stockSnapshot = stockSnapshot;
        this.demandForecastStrategy = demandForecastStrategy;
        this.criticalLotActionStrategy = criticalLotActionStrategy;
        this.clock = clock;
        this.forecastWeeks = forecastWeeks;
        this.defaultExpiryWindowDays = defaultExpiryWindowDays;
        this.defaultHorizonDays = defaultHorizonDays;
    }

    @Transactional(readOnly = true)
    @Observed(name = "insights.admin.daily")
    public DailyInsightsOutput getDailyInsights(LocalDate date, Integer expiryWindowDays, Integer horizonDays) {
        StoreCatalog.StoreRef store = storeCatalog.getDefaultStore()
                .orElseThrow(StoreNotFoundException::new);
        var output = build(store.id(), store.name(), storeNote(), date, expiryWindowDays, horizonDays);
        log.info("daily insights generated. storeId={}, targetDate={}, criticalItemsCount={}, expiringLotsCount={}, purchaseSuggestCount={}",
                output.storeId(),
                output.targetDate(),
                output.criticalStock().size(),
                output.expiringLots().size(),
                output.purchaseSuggestions().size());
        return output;
    }

    private @NonNull DailyInsightsOutput build(
            Long storeId,
            String storeName,
            String note,
            LocalDate date,
            Integer expiryWindowDays,
            Integer horizonDays
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now(clock);
        int expiryWindow = expiryWindowDays != null ? expiryWindowDays : defaultExpiryWindowDays;
        int horizon = horizonDays != null ? horizonDays : defaultHorizonDays;

        int days = demandForecastStrategy.getHistoryLookbackDays();
        LocalDateTime from = targetDate.minusDays(days).atStartOfDay();
        LocalDateTime to = targetDate.atStartOfDay();

        List<CatalogProduct> products = productCatalog.findActiveProducts();
        List<DailySoldQuantity> history = salesHistory.findDailyQuantities(from, to);
        List<ProductDemand> demand = demandForecastStrategy.forecast(
                products, history, targetDate
        );

        List<RecipeLine> recipes = recipeCatalog.findAll();
        List<StockItemSnapshot> items = stockSnapshot.findActiveItems();
        List<StockLotSnapshot> lots = stockSnapshot.findOpenLots();

        var purchases = PurchaseSuggestionCalculator.suggest(demand, recipes, items, horizon);
        var critical = CriticalStockCalculator.find(items, recipes);

        LocalDate deadline = targetDate.plusDays(expiryWindow);
        Map<Long, StockItemSnapshot> itemsById = items.stream()
                .collect(Collectors.toMap(StockItemSnapshot::id, Function.identity()));

        List<DailyInsightsOutput.ExpiringLot> expiringLots = lots.stream()
                .filter(lot -> lot.quantity() > 0)
                .filter(lot -> !lot.expiresAt().isAfter(deadline))
                .sorted(Comparator.comparing(StockLotSnapshot::expiresAt).thenComparing(StockLotSnapshot::lotId))
                .map(lot -> {
                    StockItemSnapshot item = itemsById.get(lot.stockItemId());
                    String itemName = item == null ? ("Item " + lot.stockItemId()) : item.name();
                    long daysUntilExpiry = ChronoUnit.DAYS.between(targetDate, lot.expiresAt());
                    return new DailyInsightsOutput.ExpiringLot(
                            lot.lotId(), lot.stockItemId(), itemName, lot.quantity(), lot.expiresAt(), daysUntilExpiry
                    );
                })
                .toList();

        List<InventoryAction> actions = criticalLotActionStrategy.evaluateExpiringLots(
                lots, items, recipes, demand, targetDate
        );

        List<DailyInsightsOutput.PromotionSuggestion> promotionSuggestions = actions.stream()
                .filter(PromotionSuggestion.class::isInstance)
                .map(PromotionSuggestion.class::cast)
                .map(row -> new DailyInsightsOutput.PromotionSuggestion(
                        row.stockItemId(), row.lotId(), row.expiresAt(), row.quantityInLot(),
                        row.suggestedProducts().stream().map(product -> new DailyInsightsOutput.SuggestedProduct(
                                product.productId(), product.productName(), product.predictedQuantityToday()
                        )).toList()
                ))
                .toList();

        return new DailyInsightsOutput(
                storeId,
                storeName,
                targetDate,
                targetDate.getDayOfWeek(),
                horizon,
                expiryWindow,
                new DailyInsightsOutput.Assumptions(
                        STOCK_SCOPE_GLOBAL,
                        DailyInsightsService.DEMAND_SCOPE_STORE,
                        demandForecastStrategy.getMethodName(),
                        forecastWeeks,
                        note
                ),
                demand.stream().map(row -> new DailyInsightsOutput.ProductDemand(
                        row.productId(), row.productName(), row.unitPrice(), row.predictedQuantity(),
                        row.method(), row.sampleSize(), row.confidence()
                )).toList(),
                purchases.stream().map(row -> new DailyInsightsOutput.PurchaseSuggestion(
                        row.stockItemId(), row.name(), row.unit(), row.currentQuantity(), row.minimumStock(),
                        row.predictedConsumption(), row.quantityToBuy(), row.belowMinimum(), row.affectedProductIds()
                )).toList(),
                critical.stream().map(row -> new DailyInsightsOutput.CriticalStock(
                        row.stockItemId(), row.name(), row.unit(), row.currentQuantity(),
                        row.minimumStock(), row.affectedProductIds()
                )).toList(),
                expiringLots,
                promotionSuggestions
        );
    }

    private static String storeNote() {
        return "Demanda e estoque calculados para a loja.";
    }
}
