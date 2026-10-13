package org.utl.dsm.elzarape.repository;

import org.utl.dsm.elzarape.model.Product;
import org.utl.dsm.elzarape.model.ProductType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByName();
    List<Product> findByActiveTrueAndCategoryIdOrderByName(Long categoryId);
    List<Product> findByActiveTrueAndCategoryTypeOrderByName(ProductType type);
    long countByCategoryIdAndActiveTrue(Long categoryId);
}
