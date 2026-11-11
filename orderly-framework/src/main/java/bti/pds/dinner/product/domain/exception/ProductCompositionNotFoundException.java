package bti.pds.dinner.product.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class ProductCompositionNotFoundException extends ResourceNotFoundException {
    public ProductCompositionNotFoundException(String message) {
        super("Product composition not found", message);
    }
}
