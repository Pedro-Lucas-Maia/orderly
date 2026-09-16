package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleItemOutput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.*;
import bti.pds.dinner.sales.domain.exception.InvalidSaleStateException;
import bti.pds.dinner.sales.domain.exception.SaleException;
import bti.pds.dinner.sales.domain.exception.SaleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        Map<Long, Integer> totalConsumption = calculateTotalConsumption(input.items());
        validateStockAvailability(totalConsumption);

        String reason = "Sale #" + sale.getId().uuid().toString();
        deductStock(totalConsumption, reason);

        sale.confirm();

        return toOutput(saleRepository.save(sale));
    }

    @Transactional
    public void cancelSale(String saleIdStr) {
        Sale sale = findConfirmedSale(saleIdStr);

        Map<Long, Integer> totalToReturn = calculateConsumptionFromSale(sale);

        String reason = "Cancel sale #" + saleIdStr;
        restoreStock(totalToReturn, reason);

        sale.cancel();
        saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleOutput> listSales() {
        return saleRepository.findAll()
                .stream()
                .map(this::toOutput)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleOutput getSaleById(String saleIdStr) {
        SaleId saleId = new SaleId(UUID.fromString(saleIdStr));
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleIdStr));
        return toOutput(sale);
    }

    private void validateInput(CreateSaleInput input) {
        if (input == null || input.items() == null || input.items().isEmpty()) {
            throw new SaleException("A sale must contain at least one item.");
        }
    }

    private void addItemsToSale(Sale sale, List<SaleItemInput> items) {
        for (SaleItemInput itemReq : items) {
            BigDecimal currentPrice = productRepository.getCurrentPrice(itemReq.productId());
            sale.addItem(itemReq.productId(), itemReq.quantity(), currentPrice);
        }
    }

    private Map<Long, Integer> calculateTotalConsumption(List<SaleItemInput> items) {
        Map<Long, Integer> totalConsumption = new HashMap<>();
        for (SaleItemInput itemReq : items) {
            accumulateConsumption(totalConsumption, itemReq.productId(), itemReq.quantity());
        }
        return totalConsumption;
    }

    private Map<Long, Integer> calculateConsumptionFromSale(Sale sale) {
        Map<Long, Integer> totalConsumption = new HashMap<>();
        for (SaleItem item : sale.getItems()) {
            accumulateConsumption(totalConsumption, item.getProductId(), item.getQuantity());
        }
        return totalConsumption;
    }

    private void accumulateConsumption(Map<Long, Integer> totalConsumption,
            Long productId, int quantity) {
        List<RecipeItem> recipe = productRepository.getRecipe(productId);
        for (RecipeItem ingredient : recipe) {
            int consumedQuantity = ingredient.quantityPerUnit() * quantity;
            totalConsumption.merge(ingredient.stockItemId(), consumedQuantity, Integer::sum);
        }
    }

    private void validateStockAvailability(Map<Long, Integer> totalConsumption) {
        for (Map.Entry<Long, Integer> entry : totalConsumption.entrySet()) {
            Long stockItemId = entry.getKey();
            int requiredQuantity = entry.getValue();
            int currentBalance = stockRepository.getCurrentBalance(stockItemId);

            if (currentBalance < requiredQuantity) {
                throw new InvalidSaleStateException(
                        "Insufficient stock for item: " + stockItemId
                                + ". Required quantity: " + requiredQuantity);
            }
        }
    }

    private void deductStock(Map<Long, Integer> totalConsumption, String reason) {
        for (Map.Entry<Long, Integer> entry : totalConsumption.entrySet()) {
            stockRepository.deductStock(entry.getKey(), entry.getValue(), reason);
        }
    }

    private void restoreStock(Map<Long, Integer> totalToReturn, String reason) {
        for (Map.Entry<Long, Integer> entry : totalToReturn.entrySet()) {
            stockRepository.addStock(entry.getKey(), entry.getValue(), reason);
        }
    }

    private Sale findConfirmedSale(String saleIdStr) {
        SaleId saleId = new SaleId(UUID.fromString(saleIdStr));

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleIdStr));

        if (sale.getStatus() != SaleStatus.CONFIRMED) {
            throw new InvalidSaleStateException("Only CONFIRMED sales can be cancelled.");
        }

        return sale;
    }

    private SaleOutput toOutput(Sale sale) {
        return new SaleOutput(
                sale.getId().uuid().toString(),
                sale.getDate(),
                sale.getStatus(),
                sale.calculateTotal(),
                sale.getObservation(),
                sale.getItems().stream()
                        .map(this::toItemOutput)
                        .toList()
        );
    }

    private SaleItemOutput toItemOutput(SaleItem item) {
        return new SaleItemOutput(
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
