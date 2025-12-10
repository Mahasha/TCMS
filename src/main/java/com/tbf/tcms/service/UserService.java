package com.tbf.tcms.service;

import com.tbf.tcms.domain.User;

import java.time.LocalDate;
import java.util.List;

/**
 * User Service — interface for user lifecycle operations.
 * Implementations should enforce business rules described per method.
 */
public interface UserService {

    /**
     * Disqualify a leader or council member.
     * Should set disqualification flags and remove council role if present.
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
     * Compute and appoint Top 10 council for an organization.
     */
    List<User> appointTopCouncil(Long orgId, int size);

    /**
     * Appoint an individual user to council if capacity and rules allow.
     */
    User appointUserToCouncil(Long userId);

    /**
     * Define succession heir for a leader.
     */
    User defineHeir(Long leaderId, Long heirUserId);
}
