package bti.pds.dinner.inventory.infrastructure.http.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bti.pds.dinner.inventory.application.service.InventoryService;
import bti.pds.dinner.inventory.domain.Inventory;
import bti.pds.dinner.inventory.domain.InventoryId;
import bti.pds.dinner.inventory.domain.ProductId;
import bti.pds.dinner.inventory.infrastructure.http.InventoryMapper;
import bti.pds.dinner.inventory.infrastructure.http.response.*;
import bti.pds.dinner.inventory.infrastructure.http.request.*;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable UUID inventoryId) {

        Inventory inventory = inventoryService.getInventory(
                new InventoryId(inventoryId)
        );

        return ResponseEntity.ok(
                inventoryMapper.toResponse(inventory)
        );
    }

    @PostMapping("/{inventoryId}/items")
    public ResponseEntity<InventoryResponse> addItem(
            @PathVariable UUID inventoryId,
            @RequestBody AddInventoryItemRequest request) {

        Inventory inventory = inventoryService.addItem(
                new InventoryId(inventoryId),
                new ProductId(request.productId()),
                request.quantity()
        );

        return ResponseEntity.ok(
                inventoryMapper.toResponse(inventory)
        );
    }

    @DeleteMapping("/{inventoryId}/items/{productId}")
    public ResponseEntity<InventoryResponse> removeItem(
            @PathVariable UUID inventoryId,
            @PathVariable UUID productId) {

        Inventory inventory = inventoryService.removeItem(
                new InventoryId(inventoryId),
                new ProductId(productId)
        );

        return ResponseEntity.ok(
                inventoryMapper.toResponse(inventory)
        );
    }

    @PatchMapping("/{inventoryId}/items/{productId}/increase")
    public ResponseEntity<InventoryResponse> increaseQuantity(
            @PathVariable UUID inventoryId,
            @PathVariable UUID productId,
            @RequestBody QuantityRequest request) {

        Inventory inventory = inventoryService.increaseQuantity(
                new InventoryId(inventoryId),
                new ProductId(productId),
                request.quantity()
        );

        return ResponseEntity.ok(
                inventoryMapper.toResponse(inventory)
        );
    }

    @PatchMapping("/{inventoryId}/items/{productId}/decrease")
    public ResponseEntity<InventoryResponse> decreaseQuantity(
            @PathVariable UUID inventoryId,
            @PathVariable UUID productId,
            @RequestBody QuantityRequest request) {

        Inventory inventory = inventoryService.decreaseQuantity(
                new InventoryId(inventoryId),
                new ProductId(productId),
                request.quantity()
        );

        return ResponseEntity.ok(
                inventoryMapper.toResponse(inventory)
        );
    }
}