package com.tbf.tcms.repository;

import com.tbf.tcms.domain.DisputeCase;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.domain.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeCaseRepository extends JpaRepository<DisputeCase, Long> {

    // Paging variants
    Page<DisputeCase> findAll(Pageable pageable);

    Page<DisputeCase> findByStatus(CaseStatus status, Pageable pageable);

    Page<DisputeCase> findByOrganization(Organization organization, Pageable pageable);

    Page<DisputeCase> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<DisputeCase> findByOrganizationIdAndStatus(Long organizationId, CaseStatus status, Pageable pageable);

    Page<DisputeCase> findByAccusedUserId(Long userId, Pageable pageable);

    boolean existsByAccusedUserAndStatusIn(User user, List<CaseStatus> statuses);
}
