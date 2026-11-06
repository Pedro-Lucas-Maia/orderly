package bti.pds.dinner.address.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class Address {
    private AddressId addressId;
    private UserId userId;
    private String name;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Address(UserId userId, String name, String street, String number, String complement, String neighborhood, String city, String state, String zipCode) {
        this.addressId = new AddressId();
        this.userId = userId;
        this.name = name;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public Address updateAddress(String name, String street, String number, String complement, String neighborhood, String city, String state, String zipCode) {
        this.name = name;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.updatedAt = LocalDateTime.now();

        return this;
    }
}
