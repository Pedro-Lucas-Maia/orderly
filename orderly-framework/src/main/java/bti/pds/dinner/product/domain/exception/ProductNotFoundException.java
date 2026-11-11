package bti.pds.dinner.product.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(String message) {
        super("Product not found", message);
    }
}
