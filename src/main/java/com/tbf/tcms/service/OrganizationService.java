package com.tbf.tcms.service;

import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.web.dto.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Organization Service — interface for organization hierarchy operations.
 */
public interface OrganizationService {

    /**
     * Get full hierarchy under an organization (e.g., Main Authority → all villages)
     */
    Organization getHierarchy(Long orgId);

    /**
     * Create a new organization under an optional parent.
     * Implementations should validate the parent exists when parentId is not null.
     */
    Organization createOrganization(String name, String type, Long parentId);

    /**
     * Page organizations for admin listings.
     */
    PageResponse<Organization> findAll(Pageable pageable);
}
