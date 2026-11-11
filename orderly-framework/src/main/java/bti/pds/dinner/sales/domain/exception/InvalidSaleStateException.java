package bti.pds.dinner.sales.domain.exception;

import bti.pds.dinner.common.exception.ConflictException;

public class InvalidSaleStateException extends ConflictException {
    public InvalidSaleStateException(String message) {
        super("Invalid sale state", message);
    }
}
