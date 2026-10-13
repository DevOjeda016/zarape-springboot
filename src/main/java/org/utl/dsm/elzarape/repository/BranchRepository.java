package org.utl.dsm.elzarape.repository;

import org.utl.dsm.elzarape.model.BranchOffice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<BranchOffice, Long> {
    List<BranchOffice> findByActiveTrueOrderByName();
}
