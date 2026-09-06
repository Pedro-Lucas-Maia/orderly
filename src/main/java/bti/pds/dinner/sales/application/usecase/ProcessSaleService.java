// src/main/java/bti/pds/dinner/sales/application/usecase/ProcessSaleUseCase.java
package bti.pds.dinner.sales.application.usecase;

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

@Service
public class ProcessSaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public ProcessSaleService(SaleRepository saleRepository, ProductRepository productRepository, StockRepository stockRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    // TODO: dividir esse metodo em metodos menores, pq ele tá gigante
    @Transactional
    public SaleResponse execute(CreateSaleRequest request) {
        SaleID saleId = new SaleID();
        Sale sale = new Sale(saleId, request.observation());
        
        Map<String, Integer> totalConsumption = new HashMap<>();

        for (SaleItemRequest itemReq : request.items()) {
            bti.pds.dinner.product.domain.ProductId productId = 
                new bti.pds.dinner.product.domain.ProductId(Long.parseLong(itemReq.productId()));

            BigDecimal currentPrice = productRepository.getCurrentPrice(productId.toString());
            sale.addItem(itemReq.productId(), itemReq.quantity(), currentPrice);

            List<RecipeItem> recipe = productRepository.getRecipe(productId.toString());
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
                throw new IllegalStateException("Estoque insuficiente para o item: " + stockItemId + ". Necessário: " + requiredQuantity);
            }
        }

        String reason = "Venda #" + saleId.uuid().toString();
        for (Map.Entry<String, Integer> entry : totalConsumption.entrySet()) {
            stockRepository.deductStock(entry.getKey(), entry.getValue(), reason);
        }

        sale.confirm();
        saleRepository.save(sale);

        return new SaleResponse(sale.getId().uuid().toString(), sale.calculateTotal());
    }
}