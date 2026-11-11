package bti.pds.dinner.stock.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;

public class MissingLotExpiryException extends BusinessRuleException {
    public MissingLotExpiryException() {
        super("Missing lot expiry", "Expiry date is required for ENTRADA movements");
    }
}
