package bti.pds.dinner.reports.infrastructure.http.response;

import bti.pds.dinner.reports.application.output.SalesReportOutput;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.List;

public record SalesReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        SalesReportOutput.Summary summary,
        List<SalesReportOutput.StatusBreakdown> byStatus,
        List<SalesReportOutput.DailySale> dailySeries,
        List<SalesReportOutput.ProductRanking> topProducts
) {
    public static SalesReportResponse from(@NonNull SalesReportOutput output) {
        return new SalesReportResponse(
                output.startDate(),
                output.endDate(),
                output.summary(),
                output.byStatus(),
                output.dailySeries(),
                output.topProducts()
        );
    }
}
