package bti.pds.dinner.stock.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class StockNotFoundException extends ResourceNotFoundException {
    public StockNotFoundException(String message) {
        super("Stock not found", message);
    }
}
