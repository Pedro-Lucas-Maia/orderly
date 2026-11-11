package bti.pds.dinner.reports.domain.exception;

import bti.pds.dinner.common.exception.BusinessRuleException;

public class InvalidReportPeriodException extends BusinessRuleException {
    public InvalidReportPeriodException(String message) {
        super("Invalid report period", message);
    }
}
