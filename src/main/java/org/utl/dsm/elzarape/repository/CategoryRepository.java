package org.utl.dsm.elzarape.repository;

import org.utl.dsm.elzarape.model.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByDescription();
}
