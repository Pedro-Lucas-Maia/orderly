package bti.pds.dinner.reports.application.input;

import java.time.LocalDate;

public record ReportPeriodInput(
        LocalDate startDate,
        LocalDate endDate
) {
}
