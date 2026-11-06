package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.address.application.input.AddressInput;
import bti.pds.dinner.address.application.service.AddressService;
import bti.pds.dinner.sales.domain.Address;
import bti.pds.dinner.sales.domain.SaleAddressRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class SalesSaleAddressAdapter implements SaleAddressRepository {
    private AddressService addressService;

    public SalesSaleAddressAdapter(AddressService addressService) {
        this.addressService = addressService;
    }

    @Override
    public Address getAddress(UUID addressId,  UUID userId) {
        var output = addressService.getAddress(new AddressInput(addressId, userId));

        return Address.builder()
                .addressId(output.addressId())
                .name(output.name())
                .street(output.street())
                .number(output.number())
                .neighborhood(output.neighborhood())
                .city(output.city())
                .state(output.state())
                .zipCode(output.zipCode())
                .build();
    }
}
