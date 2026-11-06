package bti.pds.dinner.product.infrastructure.http.controller;

import bti.pds.dinner.product.application.output.ProductCompositionOutput;
import bti.pds.dinner.product.application.output.ProductOutput;
import bti.pds.dinner.product.application.service.ProductCompositionService;
import bti.pds.dinner.product.application.service.ProductService;
import bti.pds.dinner.product.infrastructure.http.request.AddProductCompositionRequest;
import bti.pds.dinner.product.infrastructure.http.request.CreateProductRequest;
import bti.pds.dinner.product.infrastructure.http.request.UpdateProductCompositionRequest;
import bti.pds.dinner.product.infrastructure.http.request.UpdateProductRequest;
import bti.pds.dinner.product.infrastructure.http.response.ProductCompositionResponse;
import bti.pds.dinner.product.infrastructure.http.response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ProductCompositionService productCompositionService;

    public ProductController(
            ProductService productService,
            ProductCompositionService productCompositionService
    ) {
        this.productService = productService;
        this.productCompositionService = productCompositionService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductOutput output = productService.create(CreateProductRequest.toInput(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(output));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(@RequestParam(required = false) Boolean active) {
        List<ProductResponse> response = productService.list(active).stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        ProductOutput output = productService.getById(id);
        return ResponseEntity.ok(ProductResponse.from(output));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductOutput output = productService.update(id, UpdateProductRequest.toInput(request));
        return ResponseEntity.ok(ProductResponse.from(output));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/compositions")
    public ResponseEntity<ProductCompositionResponse> addComposition(
            @PathVariable Long productId,
            @Valid @RequestBody AddProductCompositionRequest request
    ) {
        ProductCompositionOutput output = productCompositionService.add(
                productId,
                AddProductCompositionRequest.toInput(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductCompositionResponse.from(output));
    }

    @GetMapping("/{productId}/compositions")
    public ResponseEntity<List<ProductCompositionResponse>> listCompositions(@PathVariable Long productId) {
        List<ProductCompositionResponse> response = productCompositionService.listByProduct(productId).stream()
                .map(ProductCompositionResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/compositions/{compositionId}")
    public ResponseEntity<ProductCompositionResponse> updateComposition(
            @PathVariable Long productId,
            @PathVariable Long compositionId,
            @Valid @RequestBody UpdateProductCompositionRequest request
    ) {
        ProductCompositionOutput output = productCompositionService.update(
                productId,
                compositionId,
                UpdateProductCompositionRequest.toInput(request)
        );
        return ResponseEntity.ok(ProductCompositionResponse.from(output));
    }

    @DeleteMapping("/{productId}/compositions/{compositionId}")
    public ResponseEntity<Void> deleteComposition(
            @PathVariable Long productId,
            @PathVariable Long compositionId
    ) {
        productCompositionService.delete(productId, compositionId);
        return ResponseEntity.noContent().build();
    }
}
