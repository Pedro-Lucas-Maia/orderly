package bti.pds.dinner.store.infrastructure.http.controller;

import bti.pds.dinner.store.application.output.StoreOutput;
import bti.pds.dinner.store.application.service.StoreService;
import bti.pds.dinner.store.infrastructure.http.request.UpdateStoreSettingsRequest;
import bti.pds.dinner.store.infrastructure.http.request.UpdateStoreStatusRequest;
import bti.pds.dinner.store.infrastructure.http.response.StoreResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping()
    public ResponseEntity<StoreResponse> getStore() {
        return ResponseEntity.ok(StoreResponse.from(storeService.getStore()));
    }

    @PatchMapping("/settings")
    public ResponseEntity<StoreResponse> updateSettings(@Valid @RequestBody UpdateStoreSettingsRequest request) {
        StoreOutput output = storeService.updateSettings(UpdateStoreSettingsRequest.toInput(request));
        return ResponseEntity.ok(StoreResponse.from(output));
    }

    @PutMapping("/status")
    public ResponseEntity<StoreResponse> updateStatusManually(
            @Valid @RequestBody UpdateStoreStatusRequest request
    ) {
        StoreOutput output = storeService.updateStatusManually(UpdateStoreStatusRequest.toInput(request));
        return ResponseEntity.ok(StoreResponse.from(output));
    }

    @DeleteMapping("/status/manual")
    public ResponseEntity<StoreResponse> clearManualStatus() {
        return ResponseEntity.ok(StoreResponse.from(storeService.clearManualStatus()));
    }
}
