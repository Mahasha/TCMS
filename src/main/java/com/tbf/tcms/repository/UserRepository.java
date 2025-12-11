package com.tbf.tcms.repository;

import com.tbf.tcms.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Paging queries
    Page<User> findAll(Pageable pageable);

    Page<User> findByOrganizationId(Long organizationId, Pageable pageable);

    List<User> findByLineage(String lineage);

    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.disqualified = false")
    Page<User> findEligibleUsersByOrganization(@Param("orgId") Long orgId, Pageable pageable);

    // Non-paged variant used for internal computations (e.g., appointTopCouncil)
    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.disqualified = false")
    List<User> findEligibleUsersByOrganization(@Param("orgId") Long orgId);

    @Query("SELECT u FROM User u WHERE u.heirTo.id = :userId")
    Page<User> findHeirsToUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.organization.id = :orgId AND r.name = :roleName")
    long countUsersWithRoleInOrganization(@Param("orgId") Long orgId, @Param("roleName") String roleName);

    Optional<User> findByFullName(String fullName);
}
