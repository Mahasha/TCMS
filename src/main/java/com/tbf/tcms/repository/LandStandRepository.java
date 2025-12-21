package com.tbf.tcms.repository;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.enums.StandType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LandStandRepository extends JpaRepository<LandStand, Long> {

    // Paging variants
    Page<LandStand> findAll(Pageable pageable);

    Page<LandStand> findByType(StandType type, Pageable pageable);

    Page<LandStand> findByOrganization(Organization organization, Pageable pageable);

    Page<LandStand> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<LandStand> findByAllocated(boolean allocated, Pageable pageable);

    Page<LandStand> findByAllocatedToId(Long userId, Pageable pageable);

    // Composed filters for production-grade paging (avoid in-memory filtering)
    Page<LandStand> findByAllocatedAndType(boolean allocated, StandType type, Pageable pageable);

    Page<LandStand> findByOrganizationIdAndAllocated(Long organizationId, boolean allocated, Pageable pageable);

    Page<LandStand> findByOrganizationIdAndType(Long organizationId, StandType type, Pageable pageable);

    Page<LandStand> findByOrganizationIdAndAllocatedAndType(Long organizationId, boolean allocated, StandType type, Pageable pageable);
}
