package bti.pds.dinner.address.infrastructure.http.response;

import bti.pds.dinner.address.application.output.AddressOutput;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record AddressResponse(
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
    public static AddressResponse from(@NonNull AddressOutput output) {
        return new AddressResponse(
                output.addressId(),
                output.userId(),
                output.name(),
                output.street(),
                output.number(),
                output.complement(),
                output.neighborhood(),
                output.city(),
                output.state(),
                output.zipCode()
        );
    }
}
