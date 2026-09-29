package bti.pds.dinner.address.infrastructure.persistence.entity;


import bti.pds.dinner.address.domain.Address;
import bti.pds.dinner.address.domain.AddressId;
import bti.pds.dinner.address.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_addresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressEntity {
    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    private String name;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static AddressEntity from(@NonNull Address address) {
        return AddressEntity.builder()
                .id(address.getAddressId().uuid())
                .userId(address.getUserId().uuid())
                .name(address.getName())
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    public static Address toDomain(@NonNull AddressEntity addressEntity) {
        return Address.builder()
                .addressId(new AddressId(addressEntity.getId()))
                .userId(new UserId(addressEntity.getUserId()))
                .name(addressEntity.getName())
                .street(addressEntity.getStreet())
                .number(addressEntity.getNumber())
                .complement(addressEntity.getComplement())
                .neighborhood(addressEntity.getNeighborhood())
                .city(addressEntity.getCity())
                .state(addressEntity.getState())
                .zipCode(addressEntity.getZipCode())
                .createdAt(addressEntity.getCreatedAt())
                .updatedAt(addressEntity.getUpdatedAt())
                .build();
    }
}
