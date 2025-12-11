package com.tbf.tcms.service;

import com.tbf.tcms.domain.DisputeCase;
import com.tbf.tcms.domain.enums.CaseStatus;
import com.tbf.tcms.web.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Dispute Case Service — interface to manage the lifecycle of dispute cases.
 */
public interface DisputeCaseService {

    /**
     * Open a new dispute case for an accused within an organization.
     */
    DisputeCase openCase(String description, Long accusedUserId, Long orgId);

    /**
     * File a case with an explicit complainant recorded.
     */
    DisputeCase fileCase(String description, Long complainantUserId, Long accusedUserId, Long orgId);

    /**
     * Send the next notice on the case, advancing its status through notices and referral.
     */
    DisputeCase sendNotice(Long caseId);

    /**
     * Submit a defense statement by the accused to dispute the allegations.
     */
    DisputeCase disputeCase(Long caseId, Long accusedUserId, String defenseStatement);

    /**
     * Assign adjudicators (Top 10 members from the same org) to the case.
     */
    DisputeCase assignAdjudicators(Long caseId, List<Long> adjudicatorIds);

    /**
     * Close the case.
     */
    DisputeCase closeCase(Long caseId);

    // Pagination APIs
    PageResponse<DisputeCase> findAll(Pageable pageable);

    PageResponse<DisputeCase> findByOrganization(Long organizationId, Pageable pageable);

    PageResponse<DisputeCase> findByOrganizationAndStatus(Long organizationId, CaseStatus status, Pageable pageable);

    PageResponse<DisputeCase> findByStatus(CaseStatus status, Pageable pageable);
}
