package com.tbf.tcms.repository;

import com.tbf.tcms.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByType(String type);

    @Query("SELECT o FROM Organization o WHERE o.parent.id = :parentId")
    List<Organization> findSubOrganizations(@Param("parentId") Long parentId);
}
