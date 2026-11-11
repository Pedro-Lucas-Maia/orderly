package bti.pds.dinner.sales.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;

public class OutOfDeliveryAreaException extends BusinessRuleException {
    public OutOfDeliveryAreaException(String message) {
        super("Out of delivery area", message);
    }
}
