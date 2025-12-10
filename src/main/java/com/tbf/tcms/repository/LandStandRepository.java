package com.tbf.tcms.repository;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.enums.StandType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LandStandRepository extends JpaRepository<LandStand, Long> {

    List<LandStand> findByType(StandType type);

    List<LandStand> findByOrganization(Organization organization);

    List<LandStand> findByAllocatedToId(Long userId);
}
