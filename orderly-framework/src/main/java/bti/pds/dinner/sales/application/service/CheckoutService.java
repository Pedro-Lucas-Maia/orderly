package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.application.input.CheckoutInput;
import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.Address;
import bti.pds.dinner.sales.domain.OrderPricingStrategy;
import bti.pds.dinner.sales.domain.SaleAddressRepository;
import bti.pds.dinner.sales.domain.SaleShoppingCartRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutService {
    private final SaleAddressRepository addressRepository;
    private final SaleShoppingCartRepository shoppingCartRepository;
    private final SaleService saleService;
    private final OrderPricingStrategy orderPricingStrategy;

    public CheckoutService(SaleAddressRepository addressRepository,
                           SaleShoppingCartRepository shoppingCartRepository,
                           SaleService saleService,
                           OrderPricingStrategy orderPricingStrategy
    ) {
        this.addressRepository = addressRepository;
        this.shoppingCartRepository = shoppingCartRepository;
        this.saleService = saleService;
        this.orderPricingStrategy = orderPricingStrategy;
    }

    @Transactional
    public SaleOutput execute(@NonNull CheckoutInput input) {
        List<SaleItemInput> items = shoppingCartRepository.getItems(input.shoppingCartId());
        Address address = addressRepository.getAddress(input.addressId(), input.userId());
        CreateSaleInput sale = new CreateSaleInput(
                input.observation(),
                items,
                input.userId(),
                address.getStreet(),
                address.getNumber(),
                address.getCity(),
                address.getNeighborhood(),
                address.getZipCode()
        );
        var savedSale = saleService.createSale(sale);

        shoppingCartRepository.clearItems(input.shoppingCartId());

        return savedSale;
    }

    @Transactional
    public BigDecimal simulateFreight(UUID addressId, UUID userId) {
        Address address = addressRepository.getAddress(addressId, userId);

        return orderPricingStrategy.calculateDeliveryFee(address);
    }
}
