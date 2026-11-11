package bti.pds.dinner.sales.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;

public class InvalidSaleItemException extends BusinessRuleException {
    public InvalidSaleItemException(String message) {
        super("Invalid sale item", message);
    }
}
