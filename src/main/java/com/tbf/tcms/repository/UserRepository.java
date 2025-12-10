package com.tbf.tcms.repository;

import com.tbf.tcms.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByOrganizationId(Long organizationId);

    List<User> findByLineage(String lineage);

    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.disqualified = false")
    List<User> findEligibleUsersByOrganization(@Param("orgId") Long orgId);

    @Query("SELECT u FROM User u WHERE u.heirTo.id = :userId")
    List<User> findHeirsToUser(@Param("userId") Long userId);
}
