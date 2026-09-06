package bti.pds.dinner.sales.application.usecase;

import bti.pds.dinner.sales.application.request.CreateSaleRequest;
import bti.pds.dinner.sales.application.request.SaleItemRequest;
import bti.pds.dinner.sales.application.response.SaleResponse;
import bti.pds.dinner.sales.domain.ProductRepository;
import bti.pds.dinner.sales.domain.Sale;
import bti.pds.dinner.sales.domain.SaleID;
import bti.pds.dinner.sales.domain.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProcessSaleUseCase {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public ProcessSaleUseCase(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public SaleResponse execute(CreateSaleRequest request) {

        SaleID saleId = new SaleID();

        Sale sale = new Sale(saleId, request.observation());

        for (SaleItemRequest itemReq : request.items()) {

            long rawProductId = Long.parseLong(itemReq.productId());

            bti.pds.dinner.product.domain.ProductId productId = new bti.pds.dinner.product.domain.ProductId(rawProductId);

            BigDecimal currentPrice = productRepository.getCurrentPrice(productId.toString());

            if (currentPrice == null) {
                throw new IllegalArgumentException("Product not found or price unavailable: " + itemReq.productId());
            }

            sale.addItem(itemReq.productId(), itemReq.quantity(), currentPrice);
        }

        saleRepository.save(sale);

        return new SaleResponse(sale.getId().uuid().toString(), sale.calculateTotal());
    }
}