package bti.pds.dinner.insights.application.service;

import bti.pds.dinner.insights.application.output.DailyInsightsOutput;
import bti.pds.dinner.insights.domain.*;
import bti.pds.dinner.store.domain.exception.StoreNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DailyInsightsService {

    public static final String STOCK_SCOPE_GLOBAL = "GLOBAL_SHARED";
    public static final String DEMAND_SCOPE_STORE = "STORE";
    public static final String DEMAND_SCOPE_ALL_STORES = "ALL_STORES";
    public static final String GLOBAL_STORE_NAME = "Todas as lojas";

    private final StoreCatalog storeCatalog;
    private final ConfirmedSalesHistory salesHistory;
    private final ProductCatalog productCatalog;
    private final RecipeCatalog recipeCatalog;
    private final StockSnapshot stockSnapshot;
    private final Clock clock;
    private final int forecastWeeks;
    private final int minSamplesHigh;
    private final int minSamplesMedium;
    private final int defaultExpiryWindowDays;
    private final int defaultHorizonDays;

    public DailyInsightsService(
            StoreCatalog storeCatalog,
            ConfirmedSalesHistory salesHistory,
            ProductCatalog productCatalog,
            RecipeCatalog recipeCatalog,
            StockSnapshot stockSnapshot,
            Clock clock,
            @Value("${app.insights.forecast-weeks:8}") int forecastWeeks,
            @Value("${app.insights.min-samples-high:6}") int minSamplesHigh,
            @Value("${app.insights.min-samples-medium:3}") int minSamplesMedium,
            @Value("${app.insights.expiry-window-days:3}") int defaultExpiryWindowDays,
            @Value("${app.insights.horizon-days:1}") int defaultHorizonDays
    ) {
        this.storeCatalog = storeCatalog;
        this.salesHistory = salesHistory;
        this.productCatalog = productCatalog;
        this.recipeCatalog = recipeCatalog;
        this.stockSnapshot = stockSnapshot;
        this.clock = clock;
        this.forecastWeeks = forecastWeeks;
        this.minSamplesHigh = minSamplesHigh;
        this.minSamplesMedium = minSamplesMedium;
        this.defaultExpiryWindowDays = defaultExpiryWindowDays;
        this.defaultHorizonDays = defaultHorizonDays;
    }

    @Transactional(readOnly = true)
    public DailyInsightsOutput getDailyInsights(LocalDate date, Integer expiryWindowDays, Integer horizonDays) {
        StoreCatalog.StoreRef store = storeCatalog.getDefaultStore()
                .orElseThrow(StoreNotFoundException::new);
        return build(store.id(), store.name(), DEMAND_SCOPE_STORE, storeNote(), date, expiryWindowDays, horizonDays);
    }

    private DailyInsightsOutput build(
            Long storeId,
            String storeName,
            String demandScope,
            String note,
            LocalDate date,
            Integer expiryWindowDays,
            Integer horizonDays
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now(clock);
        int expiryWindow = expiryWindowDays != null ? expiryWindowDays : defaultExpiryWindowDays;
        int horizon = horizonDays != null ? horizonDays : defaultHorizonDays;

        List<LocalDate> sampleDates = sampleWeekdays(targetDate, forecastWeeks);
        LocalDateTime from = sampleDates.isEmpty()
                ? targetDate.atStartOfDay()
                : sampleDates.getLast().atStartOfDay();
        LocalDateTime to = targetDate.atStartOfDay();

        List<CatalogProduct> products = productCatalog.findActiveProducts();
        List<DailySoldQuantity> history = salesHistory.findDailyQuantities(from, to);
        List<DemandForecastCalculator.ProductDemand> demand = DemandForecastCalculator.forecast(
                products, sampleDates, history, minSamplesHigh, minSamplesMedium
        );

        List<RecipeLine> recipes = recipeCatalog.findAll();
        List<StockItemSnapshot> items = stockSnapshot.findActiveItems();
        List<StockLotSnapshot> lots = stockSnapshot.findOpenLots();

        var purchases = PurchaseSuggestionCalculator.suggest(demand, recipes, items, horizon);
        var critical = CriticalStockCalculator.find(items, recipes);
        var expiry = ExpiryPromotionCalculator.calculate(targetDate, expiryWindow, lots, items, recipes, demand);

        return new DailyInsightsOutput(
                storeId,
                storeName,
                targetDate,
                targetDate.getDayOfWeek(),
                horizon,
                expiryWindow,
                new DailyInsightsOutput.Assumptions(
                        STOCK_SCOPE_GLOBAL,
                        demandScope,
                        DemandForecastCalculator.METHOD,
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
                expiry.expiringLots().stream().map(row -> new DailyInsightsOutput.ExpiringLot(
                        row.lotId(), row.stockItemId(), row.stockItemName(), row.quantity(),
                        row.expiresAt(), row.daysUntilExpiry()
                )).toList(),
                expiry.promotionSuggestions().stream().map(row -> new DailyInsightsOutput.PromotionSuggestion(
                        row.stockItemId(), row.lotId(), row.expiresAt(), row.quantityInLot(),
                        row.suggestedProducts().stream().map(product -> new DailyInsightsOutput.SuggestedProduct(
                                product.productId(), product.productName(), product.predictedQuantityToday()
                        )).toList()
                )).toList()
        );
    }

    static List<LocalDate> sampleWeekdays(LocalDate targetDate, int forecastWeeks) {
        List<LocalDate> dates = new ArrayList<>();
        for (int week = 1; week <= forecastWeeks; week++) {
            dates.add(targetDate.minusWeeks(week));
        }
        return dates;
    }

    private static String storeNote() {
        return "Demanda e estoque calculados para a loja.";
    }
}
