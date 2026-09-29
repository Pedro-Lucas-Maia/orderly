package bti.pds.dinner.address.infrastructure.http.controller;

import bti.pds.dinner.address.application.service.AddressService;
import bti.pds.dinner.address.infrastructure.http.request.AddressRequest;
import bti.pds.dinner.address.infrastructure.http.request.CreateAddressRequest;
import bti.pds.dinner.address.infrastructure.http.request.UpdateAddressRequest;
import bti.pds.dinner.address.infrastructure.http.response.AddressResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("/addresses")
public class AddressController {
    private final AddressService  addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse createAddress(@RequestBody @Valid CreateAddressRequest request, JwtAuthenticationToken authentication) {
        var userId = getUserId(authentication);
        var output = addressService.createAddress(CreateAddressRequest.toInput(request, userId));

        return AddressResponse.from(output);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<AddressResponse> getAllAddresses(JwtAuthenticationToken authentication) {
        var userId = getUserId(authentication);
        return addressService.getAllUserAddresses(UUID.fromString(userId))
                .stream()
                .map(AddressResponse::from)
                .toList();
    }

    @GetMapping("/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    public AddressResponse getAddress(@PathVariable String addressId, JwtAuthenticationToken authentication) {
        var userId = getUserId(authentication);
        var input = AddressRequest.toInput(addressId, userId);
        var output = addressService.getAddress(input);

        return AddressResponse.from(output);
    }

    @PutMapping("/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    public AddressResponse updateAddress(@PathVariable String addressId, @RequestBody @Valid UpdateAddressRequest request, JwtAuthenticationToken authentication) {
        var userId = getUserId(authentication);
        var input = UpdateAddressRequest.toInput(request, addressId, userId);
        var output = addressService.updateAddress(input);
        return AddressResponse.from(output);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@PathVariable String addressId, JwtAuthenticationToken authentication) {
        var userId = getUserId(authentication);
        var input =  AddressRequest.toInput(addressId, userId);
        addressService.deleteAddress(input);
    }

    private String getUserId(@NonNull JwtAuthenticationToken authentication) {
        return authentication.getToken().getClaimAsString("userId");
    }
}
