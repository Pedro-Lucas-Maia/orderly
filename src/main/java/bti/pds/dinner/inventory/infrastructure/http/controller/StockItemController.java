package bti.pds.dinner.inventory.infrastructure.http.controller;

import bti.pds.dinner.inventory.application.output.StockItemOutput;
import bti.pds.dinner.inventory.application.service.StockItemService;
import bti.pds.dinner.inventory.infrastructure.http.request.CreateStockItemRequest;
import bti.pds.dinner.inventory.infrastructure.http.request.RegisterMovementRequest;
import bti.pds.dinner.inventory.infrastructure.http.response.StockItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StockItemController {
    private final StockItemService stockItemService;

    public StockItemController(StockItemService stockItemService) {
        this.stockItemService = stockItemService;
    }

    @PostMapping("/api/stocks/{stockId}/items")
    public ResponseEntity<StockItemResponse> create(
            @PathVariable Long stockId,
            @Valid @RequestBody CreateStockItemRequest request
    ) {
        StockItemOutput output = stockItemService.create(stockId, CreateStockItemRequest.toInput(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(StockItemResponse.from(output));
    }

    @GetMapping("/api/stocks/{stockId}/items")
    public ResponseEntity<List<StockItemResponse>> listByStockId(@PathVariable Long stockId) {
        List<StockItemResponse> response = stockItemService.listByStock(stockId).stream()
                .map(StockItemResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/stock-items/{id}")
    public ResponseEntity<StockItemResponse> getById(@PathVariable Long id) {
        StockItemOutput output = stockItemService.getById(id);
        return ResponseEntity.ok(StockItemResponse.from(output));
    }

    @PostMapping("/api/stock-items/{id}/movements")
    public ResponseEntity<StockItemResponse> registerMovement(
            @PathVariable Long id,
            @Valid @RequestBody RegisterMovementRequest request
    ) {
        StockItemOutput output = stockItemService.registerMovement(id, RegisterMovementRequest.toInput(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(StockItemResponse.from(output));
    }
}
