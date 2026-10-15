package bti.pds.dinner.insights.infrastructure.http.controller;

import bti.pds.dinner.insights.application.service.DailyInsightsService;
import bti.pds.dinner.insights.infrastructure.http.response.InsightsResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class InsightsController {

    private final DailyInsightsService dailyInsightsService;

    public InsightsController(DailyInsightsService dailyInsightsService) {
        this.dailyInsightsService = dailyInsightsService;
    }

    @GetMapping({"/api/insights", "/api/store/insights"})
    public ResponseEntity<InsightsResponse> getInsights(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer expiryWindowDays,
            @RequestParam(required = false) Integer horizonDays
    ) {
        return ResponseEntity.ok(InsightsResponse.from(
                dailyInsightsService.getDailyInsights(date, expiryWindowDays, horizonDays)
        ));
    }
}
