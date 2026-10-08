package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.common.exception.ForbiddenAccessException;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.*;
import bti.pds.dinner.sales.domain.exception.SaleNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SaleStatusService {
    private final StockRepository stockRepository;
    private final SaleRepository saleRepository;
    private final RecipeConsumptionService recipeConsumptionService;

    public SaleStatusService(StockRepository stockRepository, SaleRepository saleRepository, RecipeConsumptionService recipeConsumptionService) {
        this.stockRepository = stockRepository;
        this.saleRepository = saleRepository;
        this.recipeConsumptionService = recipeConsumptionService;
    }

    @Transactional
    public SaleOutput confirmSale(String saleIdStr) {
        Sale sale = findSale(saleIdStr);

        sale.confirm();
        return SaleOutput.from(saleRepository.save(sale));
    }

    @Transactional
    public SaleOutput dispatchDelivery(String saleId, String userId) {
        Sale sale = findSale(saleId);
        checkAuthorization(sale.getUserId().uuid(), UUID.fromString(userId));
        sale.dispatch();
        return SaleOutput.from(saleRepository.save(sale));
    }

    @Transactional
    public SaleOutput confirmDelivery(String saleId, UUID userId) {
        Sale sale = findSale(saleId);
        checkAuthorization(sale.getUserId().uuid(), userId);
        sale.deliver();
        return SaleOutput.from(saleRepository.save(sale));
    }

    @Transactional
    public void cancelSale(String saleIdStr) {
        Sale sale = findSale(saleIdStr);

        sale.cancel();

        Map<Long, Integer> totalToReturn = calculateConsumptionFromSale(sale);
        restoreStock(totalToReturn, "Cancel sale #" + saleIdStr);

        saleRepository.save(sale);
    }

    private void checkAuthorization(@NonNull UUID databaseId, UUID inputId) {
        if(databaseId.compareTo(inputId) != 0) {
            throw new ForbiddenAccessException("Access denied for the requested resource");
        }
    }

    private void restoreStock(@NonNull Map<Long, Integer> totalToReturn, String reason) {
        for (Map.Entry<Long, Integer> entry : totalToReturn.entrySet()) {
            stockRepository.addStock(entry.getKey(), entry.getValue(), reason);
        }
    }

    private @NonNull Sale findSale(String saleIdStr) {
        SaleId saleId = new SaleId(UUID.fromString(saleIdStr));

        return saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleIdStr));
    }

    private @NonNull Map<Long, Integer> calculateConsumptionFromSale(@NonNull Sale sale) {
        Map<Long, Integer> totalConsumption = new HashMap<>();
        for (SaleItem item : sale.getItems()) {
            recipeConsumptionService.accumulateConsumption(totalConsumption, item.getProductId(), item.getQuantity());
        }
        return totalConsumption;
    }
}
