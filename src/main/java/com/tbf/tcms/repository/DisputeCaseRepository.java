package com.tbf.tcms.repository;

import com.tbf.tcms.domain.DisputeCase;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.domain.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeCaseRepository extends JpaRepository<DisputeCase, Long> {

    List<DisputeCase> findByStatus(CaseStatus status);

    List<DisputeCase> findByOrganization(Organization organization);

    List<DisputeCase> findByAccusedUserId(Long userId);

    boolean existsByAccusedUserAndStatusIn(User user, List<CaseStatus> statuses);
}
