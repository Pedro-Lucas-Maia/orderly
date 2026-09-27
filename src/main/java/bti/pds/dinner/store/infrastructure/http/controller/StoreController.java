package bti.pds.dinner.store.infrastructure.http.controller;

import bti.pds.dinner.store.application.output.StoreOutput;
import bti.pds.dinner.store.application.service.StoreService;
import bti.pds.dinner.store.infrastructure.http.request.CreateStoreRequest;
import bti.pds.dinner.store.infrastructure.http.request.UpdateStoreSettingsRequest;
import bti.pds.dinner.store.infrastructure.http.request.UpdateStoreStatusRequest;
import bti.pds.dinner.store.infrastructure.http.response.StoreResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody CreateStoreRequest request) {
        StoreOutput output = storeService.create(CreateStoreRequest.toInput(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(StoreResponse.from(output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(StoreResponse.from(storeService.getById(id)));
    }

    @PatchMapping("/{id}/settings")
    public ResponseEntity<StoreResponse> updateSettings(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreSettingsRequest request
    ) {
        StoreOutput output = storeService.updateSettings(id, UpdateStoreSettingsRequest.toInput(request));
        return ResponseEntity.ok(StoreResponse.from(output));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<StoreResponse> updateStatusManually(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreStatusRequest request
    ) {
        StoreOutput output = storeService.updateStatusManually(id, UpdateStoreStatusRequest.toInput(request));
        return ResponseEntity.ok(StoreResponse.from(output));
    }

    @DeleteMapping("/{id}/status/manual")
    public ResponseEntity<StoreResponse> clearManualStatus(@PathVariable Long id) {
        return ResponseEntity.ok(StoreResponse.from(storeService.clearManualStatus(id)));
    }
}
