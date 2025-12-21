package com.tbf.tcms.repository;

import com.tbf.tcms.domain.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    // Paging variants
    Page<Organization> findAll(Pageable pageable);

    Page<Organization> findByType(String type, Pageable pageable);

    @Query("SELECT o FROM Organization o WHERE o.parent.id = :parentId")
    List<Organization> findSubOrganizations(@Param("parentId") Long parentId);
}
