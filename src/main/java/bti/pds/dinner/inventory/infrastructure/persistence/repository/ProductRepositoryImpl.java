package bti.pds.dinner.inventory.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import bti.pds.dinner.inventory.domain.Product;
import bti.pds.dinner.inventory.domain.ProductId;
import bti.pds.dinner.inventory.domain.ProductRepository;
import bti.pds.dinner.inventory.infrastructure.persistence.entity.ProductEntity;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ProductRepositoryImpl implements ProductRepository{
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product){
        ProductEntity entity = toEntity(product);

        ProductEntity savedEntity = productJpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(ProductId id){
        return productJpaRepository.findById(id.uuid()).map(this::toDomain);
    }

    @Override
    public List<Product> findAll(){
        return productJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(Product product){
        productJpaRepository.deleteById(product.getProductId().uuid());
    }

    private ProductEntity toEntity(Product product){
        ProductEntity entity = new ProductEntity();

        entity.setId(product.getProductId().uuid());
        entity.setName(product.getName());
        entity.setPrice(product.getPrice());
        entity.setDescription(product.getDescription());

        return entity;
    }

    private Product toDomain(ProductEntity entity){
        Product product = new Product(
            new ProductId(entity.getId()),
            entity.getName(),
            entity.getPrice(),
            entity.getDescription()
        );

        return product;
    }
}
