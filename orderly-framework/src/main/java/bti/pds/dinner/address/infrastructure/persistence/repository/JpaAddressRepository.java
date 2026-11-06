package bti.pds.dinner.address.infrastructure.persistence.repository;

import bti.pds.dinner.address.domain.Address;
import bti.pds.dinner.address.domain.AddressId;
import bti.pds.dinner.address.domain.AddressRepository;
import bti.pds.dinner.address.domain.UserId;
import bti.pds.dinner.address.infrastructure.persistence.entity.AddressEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAddressRepository implements AddressRepository {
    private final AddressEntityRepository repository;

    public JpaAddressRepository(AddressEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Address save(Address address) {
        return AddressEntity.toDomain(repository.save(AddressEntity.from(address)));
    }

    @Override
    public Optional<Address> findById(AddressId addressId) {
        return repository.findById(addressId.uuid())
                .map(AddressEntity::toDomain);
    }

    @Override
    public List<Address> findAllByUserId(UserId userId) {
        return repository.findAllByUserId(userId.uuid())
                .stream()
                .map(AddressEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Address address) {
        repository.delete(AddressEntity.from(address));
    }
}
