package bti.pds.dinner.address.infrastructure.persistence.repository;

import bti.pds.dinner.address.infrastructure.persistence.entity.AddressEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface AddressEntityRepository extends ListCrudRepository<AddressEntity, UUID> {
    List<AddressEntity> findAllByUserId(UUID userId);
}
