package bti.pds.dinner.sales.infrastructure.http;

import bti.pds.dinner.sales.application.service.SaleService;
import bti.pds.dinner.sales.infrastructure.http.request.CreateSaleRequest;
import bti.pds.dinner.sales.infrastructure.http.response.SaleResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor 
public class SaleController {
    private final SaleService saleService;

    @PostMapping("/api/sales")
    public ResponseEntity<SaleResponse> createSale(@RequestBody CreateSaleRequest request) {
        SaleResponse response = SaleResponse.from(saleService.createSale(CreateSaleRequest.toInput(request)));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/sales")
    public List<SaleResponse> listSales() {
        return saleService.listSales()
                .stream()
                .map(SaleResponse::from)
                .toList();
    }

    @GetMapping("/api/sales/{id}")
    public SaleResponse getSaleById(@PathVariable String id) {
        return SaleResponse.from(saleService.getSaleById(id));
    }

    @PostMapping("/api/sales/{id}/cancel")
    public ResponseEntity<Void> cancelSale(@PathVariable String id) {
        saleService.cancelSale(id);
        return ResponseEntity.noContent().build();
    }
}
