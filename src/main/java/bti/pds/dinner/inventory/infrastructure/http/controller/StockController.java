package bti.pds.dinner.inventory.infrastructure.http.controller;

import bti.pds.dinner.inventory.application.output.StockOutput;
import bti.pds.dinner.inventory.application.service.StockService;
import bti.pds.dinner.inventory.infrastructure.http.request.CreateStockRequest;
import bti.pds.dinner.inventory.infrastructure.http.response.StockResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ResponseEntity<StockResponse> create(@Valid @RequestBody CreateStockRequest request) {
        StockOutput output = stockService.create(CreateStockRequest.toInput(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(StockResponse.from(output));
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> getAll() {
        List<StockResponse> response = stockService.list().stream()
                .map(StockResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> getById(@PathVariable Long id) {
        StockOutput output = stockService.getById(id);
        return ResponseEntity.ok(StockResponse.from(output));
    }
}
