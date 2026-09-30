package bti.pds.dinner.sales.infrastructure.http.controller;

import bti.pds.dinner.sales.application.service.CheckoutService;
import bti.pds.dinner.sales.application.service.SaleService;
import bti.pds.dinner.sales.infrastructure.http.request.CheckoutRequest;
import bti.pds.dinner.sales.infrastructure.http.response.SaleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService saleService;
    private final CheckoutService checkoutService;

    public SaleController(SaleService saleService, CheckoutService checkoutService) {
        this.saleService = saleService;
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<SaleResponse> processSale(@RequestBody @Valid CheckoutRequest request, JwtAuthenticationToken authentication) {
        var userId = authentication.getToken().getClaimAsString("userId");
        var output = checkoutService.execute(CheckoutRequest.toInput(request, userId));
        var response = SaleResponse.from(output);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/freight")
    public ResponseEntity<java.util.Map<String, java.math.BigDecimal>> simulateFreight(
            @RequestParam java.util.UUID addressId, 
            JwtAuthenticationToken authentication) {
        var userId = java.util.UUID.fromString(authentication.getToken().getClaimAsString("userId"));
        var fee = checkoutService.simulateFreight(addressId, userId);
        return ResponseEntity.ok(java.util.Map.of("deliveryFee", fee));
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

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SaleResponse> confirmSale(@PathVariable String id) {
        return ResponseEntity.ok(SaleResponse.from(saleService.confirmSale(id)));
    }
}
