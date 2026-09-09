package bti.pds.dinner.sales.application.input;

import java.util.List;

public record CreateSaleInput(
        String observation,
        List<SaleItemInput> items
) {
}
