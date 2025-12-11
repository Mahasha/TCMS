package com.tbf.tcms.service;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.enums.StandType;
import com.tbf.tcms.web.dto.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Land Stand Service — interface for stand applications and allocations.
 */
public interface LandStandService {

    /**
     * Allocate a stand to a specific user.
     * Use this from administrative flows (Top 10/Council assignment) when bypassing the application queue.
     */
    LandStand allocateStand(Long standId, Long userId);

    /**
     * Allow a regular user to apply for a stand. No allocation is performed here.
     */
    LandStand applyForStand(Long standId, Long userId);

    /**
     * Council (Top 10) direct assignment of a stand to a beneficiary without prior application.
     */
    LandStand assignStandByCouncil(Long standId, Long actingCouncilUserId, Long beneficiaryUserId);

    /**
     * Mark the stand fee as paid (after allocation).
     */
    LandStand markStandFeePaid(Long standId);

    // Pagination APIs
    PageResponse<LandStand> findAll(Pageable pageable);

    PageResponse<LandStand> findByAllocated(boolean allocated, Pageable pageable);

    PageResponse<LandStand> findByType(StandType type, Pageable pageable);

    PageResponse<LandStand> findByOrganization(Long organizationId, Pageable pageable);

    /**
     * Flexible search used by the grid with optional filters.
     */
    PageResponse<LandStand> search(Long organizationId, Boolean allocated, StandType type, Pageable pageable);
}
