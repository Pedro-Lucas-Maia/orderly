package bti.pds.dinner.address.application.input;

import java.util.UUID;

public record UpdateAddressInput(
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
}
