package bti.pds.dinner.address.application.output;

import bti.pds.dinner.address.domain.Address;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record AddressOutput(
        UUID addressId,
        UUID userId,
        String name,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode
) {
    public static AddressOutput from(@NonNull Address address) {
        return new AddressOutput(
                address.getAddressId().uuid(),
                address.getUserId().uuid(),
                address.getName(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode()
        );
    }
}
