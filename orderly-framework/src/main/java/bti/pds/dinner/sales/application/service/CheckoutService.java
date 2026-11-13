package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.application.input.CheckoutInput;
import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.Address;
import bti.pds.dinner.sales.domain.OrderPricingStrategy;
import bti.pds.dinner.sales.domain.SaleAddressRepository;
import bti.pds.dinner.sales.domain.SaleShoppingCartRepository;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
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
    @Observed(name = "sales.user.checkout")
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
        log.info("sale made. userId={}, saleId={}, amount={}",
                savedSale.userId(), savedSale.saleId(), savedSale.totalAmount());
        return savedSale;
    }

    @Transactional
    @Observed(name = "sales.user.simulate-freight")
    public BigDecimal simulateFreight(UUID addressId, UUID userId) {
        Address address = addressRepository.getAddress(addressId, userId);
        var deliveryFee = orderPricingStrategy.calculateDeliveryFee(address);
        log.info("Simulated freight. userId={}, addressId={}, deliveryFee={}", userId, addressId, deliveryFee);
        return deliveryFee;
    }
}
