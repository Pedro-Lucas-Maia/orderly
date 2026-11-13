package bti.pds.dinner.address.application.service;

import bti.pds.dinner.address.application.input.AddressInput;
import bti.pds.dinner.address.application.input.CreateAddressInput;
import bti.pds.dinner.address.application.input.UpdateAddressInput;
import bti.pds.dinner.address.application.output.AddressOutput;
import bti.pds.dinner.address.domain.AddressId;
import bti.pds.dinner.address.domain.AddressRepository;
import bti.pds.dinner.address.domain.UserId;
import bti.pds.dinner.address.domain.exception.AddressNotFoundException;
import bti.pds.dinner.common.exception.ForbiddenAccessException;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AddressService {
    private final AddressRepository repository;

    public AddressService(AddressRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Observed(name = "address.user.create")
    public AddressOutput createAddress(CreateAddressInput input) {
        var address = CreateAddressInput.toDomain(input);
        var savedAddress = repository.save(address);
        log.info("Address created successfully. userId={}", input.userId());
        return AddressOutput.from(savedAddress);
    }

    public AddressOutput getAddress(@NonNull AddressInput input) {
        var address = repository.findById(new AddressId(input.addressId()))
                .orElseThrow(() -> new AddressNotFoundException("Address with id " +  input.addressId() + " not found"));
        if (address.getUserId().uuid().equals(input.userId())) {
            return AddressOutput.from(address);
        }
        throw new ForbiddenAccessException("Access denied for the requested resource");
    }

    public List<AddressOutput> getAllUserAddresses(UUID userId) {
        return repository.findAllByUserId(new UserId(userId))
                .stream()
                .map(AddressOutput::from)
                .toList();
    }

    @Transactional
    @Observed(name = "address.user.update")
    public AddressOutput updateAddress(@NonNull UpdateAddressInput input) {
        var address = repository.findById(new AddressId(input.addressId()))
                .orElseThrow(() -> new AddressNotFoundException("Address with id " +  input.addressId() + " not found"));
        if (address.getUserId().uuid().equals(input.userId())) {
            log.info("Updated user address successfully. userId={}", input.userId());
            return AddressOutput.from(address.updateAddress(
                    input.name(),
                    input.street(),
                    input.number(),
                    input.complement(),
                    input.neighborhood(),
                    input.city(),
                    input.state(),
                    input.zipCode()));
        }
        throw new ForbiddenAccessException("Access denied for the requested resource");
    }

    @Transactional
    @Observed(name = "address.user.delete")
    public void deleteAddress(@NonNull AddressInput input) {
        var address = repository.findById(new AddressId(input.addressId()))
                .orElseThrow(() -> new AddressNotFoundException("Address with id " +  input.addressId() + " not found"));

        if (address.getUserId().uuid().equals(input.userId())) {
            log.info("Deleted user address successfully. userId={}", input.userId());
            repository.delete(address);
        } else {
            throw new ForbiddenAccessException("Access denied for the requested resource");
        }
    }
}
