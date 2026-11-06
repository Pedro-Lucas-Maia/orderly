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
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {
    private final AddressRepository repository;

    public AddressService(AddressRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AddressOutput createAddress(CreateAddressInput input) {
        var address = CreateAddressInput.toDomain(input);
        var savedAddress = repository.save(address);

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
    public AddressOutput updateAddress(@NonNull UpdateAddressInput input) {
        var address = repository.findById(new AddressId(input.addressId()))
                .orElseThrow(() -> new AddressNotFoundException("Address with id " +  input.addressId() + " not found"));
        if (address.getUserId().uuid().equals(input.userId())) {
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
    public void deleteAddress(@NonNull AddressInput input) {
        var address = repository.findById(new AddressId(input.addressId()))
                .orElseThrow(() -> new AddressNotFoundException("Address with id " +  input.addressId() + " not found"));

        if (address.getUserId().uuid().equals(input.userId())) {
            repository.delete(address);
        } else {
            throw new ForbiddenAccessException("Access denied for the requested resource");
        }
    }
}
