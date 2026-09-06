package bti.pds.dinner.sales.infrastructure.http;

import org.springframework.web.bind.annotation.RestController;

import bti.pds.dinner.sales.application.request.CreateSaleRequest;
import bti.pds.dinner.sales.application.response.SaleResponse;
import bti.pds.dinner.sales.application.service.SaleService;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@AllArgsConstructor 
public class SaleController {
    private final SaleService saleService;

    @PostMapping("/api/sales")
    public ResponseEntity<SaleResponse> createSale(@RequestBody CreateSaleRequest request) {
        SaleResponse response = saleService.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

     // TODO: listSales
     // TODO: getSaleById
     // TODO: cancelSale
}
