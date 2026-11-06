package bti.pds.dinner.sales.domain;

import java.util.UUID;

public interface SaleAddressRepository {
    Address getAddress(UUID addressId, UUID userId);
}
