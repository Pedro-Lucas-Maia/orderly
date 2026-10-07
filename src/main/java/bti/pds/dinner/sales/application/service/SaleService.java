package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.*;
import bti.pds.dinner.sales.domain.exception.*;
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
    private final StoreAvailability storeAvailability;
    private final RecipeConsumptionService recipeConsumptionService;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository,
            StockRepository stockRepository, StoreAvailability storeAvailability,
            RecipeConsumptionService recipeConsumptionService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.storeAvailability = storeAvailability;
        this.recipeConsumptionService = recipeConsumptionService;
    }

    @Transactional
    public SaleOutput createSale(CreateSaleInput input) {
        validateInput(input);
        ensureStoreIsAvailable(input.storeId());
        BigDecimal deliveryFee = calculateDeliveryFee(input.deliveryStreet(), input.deliveryNumber(), input.deliveryNeighborhood(), input.deliveryCity(), input.deliveryZipCode());

        Sale sale = new Sale(input.storeId(), input.observation(), new UserId(input.userId()), deliveryFee, input.deliveryStreet(), input.deliveryNumber(), input.deliveryNeighborhood(), input.deliveryCity(), input.deliveryZipCode());
        addItemsToSale(sale, input.items());

        Map<Long, Integer> totalConsumption = calculateTotalConsumption(input.items());
        validateStockAvailability(totalConsumption);
        deductStock(totalConsumption, "Sale #" + sale.getId().uuid());
        return SaleOutput.from(saleRepository.save(sale));
    }

    public BigDecimal calculateDeliveryFee(String street, String number, String neighborhood, String city, String zipCode) {
        if (city == null || !city.trim().equalsIgnoreCase("Natal")) {
            throw new OutOfDeliveryAreaException("Unfortunately, we do not deliver to the city: " + city);
        }

        String normalizedNeighborhood = neighborhood != null ? neighborhood.trim().toLowerCase() : "";
        
        return switch (normalizedNeighborhood) {
            case "ponta negra", "capim macio", "neópolis" -> new BigDecimal("5.00");
            case "candelária", "lagoa nova", "nova descoberta", "tirol", "petrópolis" -> new BigDecimal("7.50");
            case "alecrim", "quintas", "bairro das roças", "areia preta", "mãe luiza" -> new BigDecimal("10.00");
            case "zona norte", "potengi", "pajuçara", "redinha", "igapó" -> new BigDecimal("15.00");
            default -> new BigDecimal("10.00"); // Valor padrão para bairros não listados
        };
    }

    @Transactional(readOnly = true)
    public List<SaleOutput> listSales() {
        return saleRepository.findAll()
                .stream()
                .map(SaleOutput::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleOutput getSaleById(String saleIdStr) {
        SaleId saleId = new SaleId(UUID.fromString(saleIdStr));
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleIdStr));
        return SaleOutput.from(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleOutput> ListSalesByUser(String userId) {
        return saleRepository.findByUserId(new UserId(UUID.fromString(userId)))
                .stream()
                .map(SaleOutput::from)
                .toList();
    }

    private void validateInput(CreateSaleInput input) {
        if (input == null || input.items() == null || input.items().isEmpty()) {
            throw new SaleException("A sale must contain at least one item.");
        }
    }

    private void ensureStoreIsAvailable(Long storeId) {
        if (!storeAvailability.isAvailableForOrders(storeId)) {
            throw new StoreUnavailableException(storeId);
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
            recipeConsumptionService.accumulateConsumption(totalConsumption, itemReq.productId(), itemReq.quantity());
        }
        return totalConsumption;
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
}
