package bti.pds.dinner.stock.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class StockItemNotFoundException extends ResourceNotFoundException {
    public StockItemNotFoundException(String message) {
        super("Stock item not found", message);
    }
}
