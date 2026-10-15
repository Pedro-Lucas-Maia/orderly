package bti.pds.dinner.sales.application.input;

import java.util.List;
import java.util.UUID;

public record CreateSaleInput(
        String observation,
        List<SaleItemInput> items,
        UUID userId,
        String deliveryStreet,
        String deliveryNumber,
        String deliveryCity,
        String deliveryNeighborhood,
        String deliveryZipCode
) {
}
