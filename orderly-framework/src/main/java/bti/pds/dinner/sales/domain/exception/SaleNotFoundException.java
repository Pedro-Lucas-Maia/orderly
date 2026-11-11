package bti.pds.dinner.sales.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class SaleNotFoundException extends ResourceNotFoundException {
    public SaleNotFoundException(String message) {
        super("Sale not found", message);
    }
}
