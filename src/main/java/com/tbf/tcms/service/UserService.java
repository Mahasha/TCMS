package com.tbf.tcms.service;

import com.tbf.tcms.domain.User;
import com.tbf.tcms.web.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * User Service — interface for user lifecycle operations.
 * Implementations should enforce business rules described per method.
 */
public interface UserService {

    /**
     * Disqualify a leader or council member.
     * Should set disqualification flags and remove a council role if present.
     */
    User disqualifyUser(Long userId, String reason);

    /**
     * Create a new user under an organization.
     */
    User createUser(String fullName, String lineage, Long organizationId, LocalDate birthDate);

    /**
     * Assign a named role to a user (idempotent add-only).
     */
    User assignRoleToUser(Long userId, String roleName);

    /**
     * Compute and appoint a Top 10 council for an organization.
     */
    List<User> appointTopCouncil(Long orgId, int size);

    /**
     * Appoint an individual user to a council if capacity and rules allow.
     */
    User appointUserToCouncil(Long userId);

    /**
     * Define succession heir for a leader.
     */
    User defineHeir(Long leaderId, Long heirUserId);

    /**
     * Page all users with sorting. Useful for admin grids.
     */
    PageResponse<User> findAll(Pageable pageable);

    /**
     * Page users within an organization (village/authority).
     */
    PageResponse<User> findByOrganization(Long organizationId, Pageable pageable);

    /**
     * Page eligible council members (not disqualified) in an organization.
     * Example: Ntona forming the Top 10 council.
     */
    PageResponse<User> findEligibleCouncilByOrganization(Long organizationId, Pageable pageable);
}
