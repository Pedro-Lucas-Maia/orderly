package bti.pds.dinner.reports.infrastructure.http.controller;

import bti.pds.dinner.reports.application.input.ReportPeriodInput;
import bti.pds.dinner.reports.application.service.ReportService;
import bti.pds.dinner.reports.infrastructure.http.response.SalesReportResponse;
import bti.pds.dinner.reports.infrastructure.http.response.StockReportResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    @ResponseStatus(HttpStatus.OK)
    public SalesReportResponse getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        var output = reportService.generateSalesReport(new ReportPeriodInput(startDate, endDate));
        return SalesReportResponse.from(output);
    }

    @GetMapping("/stock")
    @ResponseStatus(HttpStatus.OK)
    public StockReportResponse getStockReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        var output = reportService.generateStockReport(new ReportPeriodInput(startDate, endDate));
        return StockReportResponse.from(output);
    }
}
