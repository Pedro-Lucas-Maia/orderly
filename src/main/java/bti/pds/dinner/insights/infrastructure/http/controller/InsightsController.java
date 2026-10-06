package bti.pds.dinner.insights.infrastructure.http.controller;

import bti.pds.dinner.insights.application.service.DailyInsightsService;
import bti.pds.dinner.insights.infrastructure.http.response.InsightsResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class InsightsController {

    private final DailyInsightsService dailyInsightsService;

    public InsightsController(DailyInsightsService dailyInsightsService) {
        this.dailyInsightsService = dailyInsightsService;
    }

    @GetMapping("/api/stores/{storeId}/insights")
    public ResponseEntity<InsightsResponse> getByStoreId(
            @PathVariable Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer expiryWindowDays,
            @RequestParam(required = false) Integer horizonDays
    ) {
        return ResponseEntity.ok(InsightsResponse.from(
                dailyInsightsService.getForStore(storeId, date, expiryWindowDays, horizonDays)
        ));
    }

    // TODO: listagem global provisória — remover quando o front passar a usar o storeId
    // (mesmo padrão de GET /api/stock-items). A demanda aqui soma vendas de todas as lojas;
    // o estoque continua compartilhado.
    @GetMapping("/api/insights")
    public ResponseEntity<InsightsResponse> getGlobal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer expiryWindowDays,
            @RequestParam(required = false) Integer horizonDays
    ) {
        return ResponseEntity.ok(InsightsResponse.from(
                dailyInsightsService.getGlobal(date, expiryWindowDays, horizonDays)
        ));
    }
}
