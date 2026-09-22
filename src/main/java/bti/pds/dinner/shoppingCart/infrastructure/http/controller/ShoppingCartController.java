package bti.pds.dinner.shoppingCart.infrastructure.http.controller;

import bti.pds.dinner.shoppingCart.application.input.DeleteItemInput;
import bti.pds.dinner.shoppingCart.application.service.ShoppingCartService;
import bti.pds.dinner.shoppingCart.infrastructure.http.request.AddItemRequest;
import bti.pds.dinner.shoppingCart.infrastructure.http.request.UpdateItemQuantityRequest;
import bti.pds.dinner.shoppingCart.infrastructure.http.response.CartResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/cart")
public class ShoppingCartController {
    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.cartService = shoppingCartService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public CartResponse addItem(@RequestBody @Valid AddItemRequest request, JwtAuthenticationToken authentication) {
        String userId = getUserId(authentication);
        return CartResponse.from(cartService.addItem(AddItemRequest.from(request, userId)));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public CartResponse getCart(JwtAuthenticationToken authentication) {
        String userId = getUserId(authentication);
        return CartResponse.from(cartService.getCart(userId));
    }

    @PatchMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public CartResponse updateCart(@PathVariable String productId, @RequestBody UpdateItemQuantityRequest request, JwtAuthenticationToken authentication) {
        String userId = getUserId(authentication);
        return CartResponse.from(cartService.updateItemQuantity(UpdateItemQuantityRequest.toInput(request, productId, userId)));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public CartResponse deleteItem(@PathVariable String productId, JwtAuthenticationToken authentication) {
        String userId = getUserId(authentication);
        return CartResponse.from(cartService.deleteItem(new DeleteItemInput(userId, productId)));
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.OK)
    public CartResponse clearCart(JwtAuthenticationToken authentication) {
        String userId = getUserId(authentication);
        return CartResponse.from(cartService.clearCart(userId));
    }

    private String getUserId(@NonNull JwtAuthenticationToken authentication) {
        return authentication.getToken().getClaimAsString("userId");
    }
}
