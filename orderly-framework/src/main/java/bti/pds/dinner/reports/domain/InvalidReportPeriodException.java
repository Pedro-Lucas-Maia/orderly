package bti.pds.dinner.reports.domain;

public class InvalidReportPeriodException extends RuntimeException {
    public InvalidReportPeriodException(String message) {
        super(message);
    }
}
