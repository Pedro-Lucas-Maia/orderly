package bti.pds.dinner.sales.infrastructure.http.controller;

import bti.pds.dinner.sales.application.service.SaleService;
import bti.pds.dinner.sales.infrastructure.http.request.CreateSaleRequest;
import bti.pds.dinner.sales.infrastructure.http.response.SaleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/sales")
public class SaleController {
    
    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@RequestBody @Valid CreateSaleRequest request) {
        SaleResponse response = SaleResponse.from(saleService.createSale(CreateSaleRequest.toInput(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>> listSales() {
        List<SaleResponse> responses = saleService.listSales()
                .stream()
                .map(SaleResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable String id) {
        SaleResponse response = SaleResponse.from(saleService.getSaleById(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSale(@PathVariable String id) {
        saleService.cancelSale(id);
        return ResponseEntity.noContent().build();
    }
}
