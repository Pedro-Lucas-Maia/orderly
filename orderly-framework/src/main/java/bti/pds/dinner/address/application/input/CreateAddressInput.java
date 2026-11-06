package bti.pds.dinner.address.application.input;

import bti.pds.dinner.address.domain.Address;
import bti.pds.dinner.address.domain.UserId;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record CreateAddressInput(
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
    public static Address toDomain(@NonNull CreateAddressInput input) {
        return new Address(
                new UserId(input.userId()),
                input.name(),
                input.street(),
                input.number(),
                input.complement(),
                input.neighborhood(),
                input.city(),
                input.state(),
                input.zipCode()
        );
    }
}
