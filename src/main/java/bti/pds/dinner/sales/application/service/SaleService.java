package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.product.domain.ProductId;
import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository,
            StockRepository stockRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public SaleOutput createSale(CreateSaleInput input) {
        validateInput(input);

        Sale sale = new Sale(input.observation());
        addItemsToSale(sale, input.items());

        Map<String, Integer> totalConsumption = calculateTotalConsumption(input.items());
        validateStockAvailability(totalConsumption);

        String reason = "Sale #" + sale.getId().uuid().toString();
        deductStock(totalConsumption, reason);

        sale.confirm();


        return SaleOutput.from(saleRepository.save(sale)    , LocalDateTime.now());
    }

    @Transactional
    public void cancelSale(String saleIdStr) {
        Sale sale = findConfirmedSale(saleIdStr);

        Map<String, Integer> totalToReturn = calculateConsumptionFromSale(sale);

        String reason = "Cancel sale #" + saleIdStr;
        restoreStock(totalToReturn, reason);

        sale.cancel();
        saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleOutput> listSales() {
        return saleRepository.findAll()
                .stream()
                .map(sale -> SaleOutput.from(sale, LocalDateTime.now()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleOutput getSaleById(String saleIdStr) {
        SaleID saleId = new SaleID(UUID.fromString(saleIdStr));
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + saleIdStr));
        return SaleOutput.from(sale, LocalDateTime.now());
    }


    private void validateInput(CreateSaleInput input) {
        if (input == null || input.items() == null || input.items().isEmpty()) {
            throw new IllegalArgumentException("A sale must contain at least one item.");
        }
    }


    private void addItemsToSale(Sale sale, List<SaleItemInput> items) {
        for (SaleItemInput itemReq : items) {
            String productId = resolveProductId(itemReq.productId());
            BigDecimal currentPrice = productRepository.getCurrentPrice(productId);
            sale.addItem(itemReq.productId(), itemReq.quantity(), currentPrice);
        }
    }

    private Map<String, Integer> calculateTotalConsumption(List<SaleItemInput> items) {
        Map<String, Integer> totalConsumption = new HashMap<>();
        for (SaleItemInput itemReq : items) {
            String productId = resolveProductId(itemReq.productId());
            accumulateConsumption(totalConsumption, productId, itemReq.quantity());
        }
        return totalConsumption;
    }

    private Map<String, Integer> calculateConsumptionFromSale(Sale sale) {
        Map<String, Integer> totalConsumption = new HashMap<>();
        for (SaleItem item : sale.getItems()) {
            String productId = resolveProductId(item.getProductId());
            accumulateConsumption(totalConsumption, productId, item.getQuantity());
        }
        return totalConsumption;
    }

    private void accumulateConsumption(Map<String, Integer> totalConsumption,
            String productId, int quantity) {
        List<RecipeItem> recipe = productRepository.getRecipe(productId);
        for (RecipeItem ingredient : recipe) {
            int consumedQuantity = ingredient.quantityPerUnit() * quantity;
            totalConsumption.merge(ingredient.stockItemId(), consumedQuantity, Integer::sum);
        }
    }

    private void validateStockAvailability(Map<String, Integer> totalConsumption) {
        for (Map.Entry<String, Integer> entry : totalConsumption.entrySet()) {
            String stockItemId = entry.getKey();
            int requiredQuantity = entry.getValue();
            int currentBalance = stockRepository.getCurrentBalance(stockItemId);

            if (currentBalance < requiredQuantity) {
                throw new IllegalStateException(
                        "Insufficient stock for item: " + stockItemId
                                + ". Required quantity: " + requiredQuantity);
            }
        }
    }

    private void deductStock(Map<String, Integer> totalConsumption, String reason) {
        for (Map.Entry<String, Integer> entry : totalConsumption.entrySet()) {
            stockRepository.deductStock(entry.getKey(), entry.getValue(), reason);
        }
    }

    private void restoreStock(Map<String, Integer> totalToReturn, String reason) {
        for (Map.Entry<String, Integer> entry : totalToReturn.entrySet()) {
            stockRepository.addStock(entry.getKey(), entry.getValue(), reason);
        }
    }

    private Sale findConfirmedSale(String saleIdStr) {
        SaleID saleId = new SaleID(UUID.fromString(saleIdStr));

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + saleIdStr));

        if (sale.getStatus() != SaleStatus.CONFIRMADA) {
            throw new IllegalStateException("Only CONFIRMADA sales can be cancelled.");
        }

        return sale;
    }

    private String resolveProductId(String rawProductId) {
        ProductId productId = new ProductId(Long.parseLong(rawProductId));
        return productId.value().toString();
    }
}
