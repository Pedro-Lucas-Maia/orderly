package bti.pds.dinner.sales.application.input;

import java.util.List;

public record CreateSaleInput(
        Long storeId,
        String observation,
        List<SaleItemInput> items
) {
}
