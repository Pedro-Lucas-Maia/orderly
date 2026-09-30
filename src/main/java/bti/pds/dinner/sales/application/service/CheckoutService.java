package bti.pds.dinner.sales.application.service;

import bti.pds.dinner.sales.application.input.CheckoutInput;
import bti.pds.dinner.sales.application.input.CreateSaleInput;
import bti.pds.dinner.sales.application.input.SaleItemInput;
import bti.pds.dinner.sales.application.output.SaleOutput;
import bti.pds.dinner.sales.domain.Address;
import bti.pds.dinner.sales.domain.SaleAddressRepository;
import bti.pds.dinner.sales.domain.SaleShoppingCartRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckoutService {
    private final SaleAddressRepository addressRepository;
    private final SaleShoppingCartRepository shoppingCartRepository;
    private final SaleService saleService;

    public CheckoutService(SaleAddressRepository addressRepository, SaleShoppingCartRepository shoppingCartRepository, SaleService saleService) {
        this.addressRepository = addressRepository;
        this.shoppingCartRepository = shoppingCartRepository;
        this.saleService = saleService;
    }

    @Transactional
    public SaleOutput execute(@NonNull CheckoutInput input) {
        List<SaleItemInput> items = shoppingCartRepository.getItems(input.shoppingCartId());
        Address address = addressRepository.getAddress(input.addressId(), input.userId());
        CreateSaleInput sale = new CreateSaleInput(
               input.storeId(),
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
    public java.math.BigDecimal simulateFreight(java.util.UUID addressId, java.util.UUID userId) {
        Address address = addressRepository.getAddress(addressId, userId);
        return saleService.calculateDeliveryFee(address.getStreet(), address.getNumber(), address.getNeighborhood(), address.getCity(), address.getZipCode());
    }
}
