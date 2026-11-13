package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.common.exception.ForbiddenAccessException;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.*;
import bti.pds.dinner.sales.domain.exception.SaleNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SaleStatusService {
    private final StockRepository stockRepository;
    private final SaleRepository saleRepository;
    private final RecipeConsumptionService recipeConsumptionService;
    private final SaleConsumptionStrategy saleConsumptionStrategy;

    public SaleStatusService(StockRepository stockRepository, SaleRepository saleRepository,
                             RecipeConsumptionService recipeConsumptionService, SaleConsumptionStrategy saleConsumptionStrategy) {
        this.stockRepository = stockRepository;
        this.saleRepository = saleRepository;
        this.recipeConsumptionService = recipeConsumptionService;
        this.saleConsumptionStrategy = saleConsumptionStrategy;
    }

    @Transactional
    @Observed(name = "sales.admin.confirm-sale")
    public SaleOutput confirmSale(String saleIdStr) {
        Sale sale = findSale(saleIdStr);

        sale.confirm();
        var savedSale = saleRepository.save(sale);
        log.info("sale started preparing. userId={}, saleId={}", savedSale.getUserId(), savedSale.getId());
        return SaleOutput.from(savedSale);
    }

    @Transactional
    @Observed(name = "sales.admin.distach-sale")
    public SaleOutput dispatchDelivery(String saleId) {
        Sale sale = findSale(saleId);
        sale.dispatch();
        var savedSale = saleRepository.save(sale);
        log.info("sale dispatched. userId={}, saleId={}", savedSale.getUserId(), savedSale.getId());
        return SaleOutput.from(savedSale);
    }

    @Transactional
    @Observed(name = "sales.user.confirm-delivery")
    public SaleOutput confirmDelivery(String saleId, UUID userId, boolean isRequestFromAdmin) {
        Sale sale = findSale(saleId);
        if (isNotARequestFromAdmin(isRequestFromAdmin)) {
            checkAuthorization(sale.getUserId().uuid(), userId);
        }
        sale.deliver();
        var savedSale = saleRepository.save(sale);
        log.info("sale delivered. userId={}, saleId={}", savedSale.getUserId(), savedSale.getId());
        return SaleOutput.from(savedSale);
    }
    private boolean isNotARequestFromAdmin(boolean isRequestFromAdmin) {
        return !isRequestFromAdmin;
    }

    @Transactional
    @Observed(name = "sales.user.cancel")
    public void cancelSale(String saleIdStr) {
        Sale sale = findSale(saleIdStr);
        SaleStatus statusBeforeCancel = sale.getStatus();

        sale.cancel(saleConsumptionStrategy);

        if (saleConsumptionStrategy.shouldRestoreOnCancellation(statusBeforeCancel)) {
            Map<Long, Integer> totalToReturn = calculateConsumptionFromSale(sale);
            restoreStock(totalToReturn, "Cancel sale #" + saleIdStr);
        }

        saleRepository.save(sale);
        log.warn("sale canceled. userId={}, saleId={}", sale.getUserId(), sale.getId());
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
