package bti.pds.dinner.address.domain;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    Address save(Address address);
    Optional<Address> findById(AddressId addressId);
    List<Address> findAllByUserId(UserId userId);
    void delete(Address address);
}
