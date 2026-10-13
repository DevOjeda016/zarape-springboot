package org.utl.dsm.elzarape.repository;

import org.utl.dsm.elzarape.model.CustomerOrder;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = {"client", "branch", "items"})
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"client", "branch", "items"})
    List<CustomerOrder> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
