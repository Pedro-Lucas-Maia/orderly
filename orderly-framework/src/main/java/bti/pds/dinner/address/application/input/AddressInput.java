package bti.pds.dinner.address.application.input;

import java.util.UUID;

public record AddressInput(
        UUID addressId,
        UUID userId
) {
}
