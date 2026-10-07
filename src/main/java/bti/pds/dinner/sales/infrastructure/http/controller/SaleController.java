package bti.pds.dinner.sales.infrastructure.http.controller;

import bti.pds.dinner.sales.application.service.CheckoutService;
import bti.pds.dinner.sales.application.service.SaleService;
import bti.pds.dinner.sales.application.service.SaleStatusService;
import bti.pds.dinner.sales.infrastructure.http.request.CheckoutRequest;
import bti.pds.dinner.sales.infrastructure.http.response.SaleResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService saleService;
    private final CheckoutService checkoutService;
    private final SaleStatusService saleStatusService;

    public SaleController(SaleService saleService, CheckoutService checkoutService, SaleStatusService saleStatusService) {
        this.saleService = saleService;
        this.checkoutService = checkoutService;
        this.saleStatusService = saleStatusService;
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
            @RequestParam UUID addressId,
            JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getClaimAsString("userId"));
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

    @GetMapping("/me")
    public ResponseEntity<List<SaleResponse>> getSaleByUserId(@NonNull JwtAuthenticationToken authentication) {
        var user =  authentication.getToken().getClaimAsString("userId");
        var response = saleService.ListSalesByUser(user)
                .stream()
                .map(SaleResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSale(@PathVariable String id) {
        saleStatusService.cancelSale(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SaleResponse> confirmSale(@PathVariable String id) {
        return ResponseEntity.ok(SaleResponse.from(saleStatusService.confirmSale(id)));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<SaleResponse> deliverSale(@PathVariable String id, @NonNull JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getClaimAsString("userId"));

        return ResponseEntity.ok(SaleResponse.from(saleStatusService.confirmDelivery(id, userId)));
    }
}
