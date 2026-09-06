// src/main/java/bti/pds/dinner/sales/application/usecase/ProcessSaleUseCase.java
package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.application.request.CreateSaleRequest;
import bti.pds.dinner.sales.application.request.SaleItemRequest;
import bti.pds.dinner.sales.application.response.SaleResponse;
import bti.pds.dinner.sales.domain.*;
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
    public SaleResponse execute(CreateSaleRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("A sale must contain at least one item.");
        }
        SaleID saleId = new SaleID();
        Sale sale = new Sale(saleId, request.observation());

        Map<String, Integer> totalConsumption = new HashMap<>();

        for (SaleItemRequest itemReq : request.items()) {
            bti.pds.dinner.product.domain.ProductId productId = new bti.pds.dinner.product.domain.ProductId(
                    Long.parseLong(itemReq.productId()));

            String productIdValue = productId.value().toString();
            BigDecimal currentPrice = productRepository.getCurrentPrice(productIdValue);
            sale.addItem(itemReq.productId(), itemReq.quantity(), currentPrice);

            List<RecipeItem> recipe = productRepository.getRecipe(productIdValue);
            for (RecipeItem ingredient : recipe) {
                int consumedQuantity = ingredient.quantityPerUnit() * itemReq.quantity();

                totalConsumption.merge(ingredient.stockItemId(), consumedQuantity, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> entry : totalConsumption.entrySet()) {
            String stockItemId = entry.getKey();
            int requiredQuantity = entry.getValue();

            int currentBalance = stockRepository.getCurrentBalance(stockItemId);

            if (currentBalance < requiredQuantity) {
                throw new IllegalStateException(
                        "Insufficient stock for item: " + stockItemId + ". Required quantity: " + requiredQuantity);
            }
        }

        String reason = "Sale #" + saleId.uuid().toString();
        for (Map.Entry<String, Integer> entry : totalConsumption.entrySet()) {
            stockRepository.deductStock(entry.getKey(), entry.getValue(), reason);
        }

        sale.confirm();
        saleRepository.save(sale);

        return toResponse(sale);
    }

    @Transactional
    public void cancelSale(String saleIdStr) {
        SaleID saleId = new SaleID(UUID.fromString(saleIdStr));

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + saleIdStr));

        if (sale.getStatus() != SaleStatus.CONFIRMADA) {
            throw new IllegalStateException("Only CONFIRMADA sales can be cancelled.");
        }

        Map<String, Integer> totalToReturn = new HashMap<>();

        for (SaleItem item : sale.getItems()) {
            bti.pds.dinner.product.domain.ProductId productId = new bti.pds.dinner.product.domain.ProductId(
                    Long.parseLong(item.getProductId()));

            List<RecipeItem> recipe = productRepository.getRecipe(productId.value().toString());

            for (RecipeItem ingredient : recipe) {
                int quantityToReturn = ingredient.quantityPerUnit() * item.getQuantity();
                totalToReturn.merge(ingredient.stockItemId(), quantityToReturn, Integer::sum);
            }
        }

        String reason = "Cancel sale #" + saleIdStr;
        for (Map.Entry<String, Integer> entry : totalToReturn.entrySet()) {
            stockRepository.addStock(entry.getKey(), entry.getValue(), reason);
        }

        sale.cancel();
        saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> listSales() {
        List<Sale> sales = saleRepository.findAll();

        return sales.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(String saleIdStr) {
        SaleID saleId = new SaleID(UUID.fromString(saleIdStr));
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + saleIdStr));
        return toResponse(sale);
    }

    private SaleResponse toResponse(Sale sale) {
        return new SaleResponse(
                sale.getId().uuid().toString(),
                sale.getDate(),
                sale.getStatus(),
                sale.calculateTotal(),
                sale.getObservation(),
                sale.getItems().stream()
                        .map(item -> new bti.pds.dinner.sales.application.response.SaleItemResponse(
                                item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                        .toList());
    }
}
