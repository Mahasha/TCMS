package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.Role;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.repository.LandStandRepository;
import com.tbf.tcms.repository.RoleRepository;
import com.tbf.tcms.repository.UserRepository;
import com.tbf.tcms.service.LandStandService;
import com.tbf.tcms.web.dto.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import com.tbf.tcms.domain.enums.StandType;

@Service
@RequiredArgsConstructor
@Transactional
public class LandStandServiceImpl implements LandStandService {

    private final LandStandRepository landStandRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Allocate a stand to a specific user.
     * Use this from administrative flows (Top 10/Council assignment) when bypassing the application queue.
     * Technical note: We intentionally keep this low-level and perform validations in caller methods.
     */
    @Override
    public LandStand allocateStand(Long standId, Long userId) {
        LandStand stand = landStandRepository.findById(standId)
                .orElseThrow(() -> new EntityNotFoundException("Stand not found"));

        if (stand.isAllocated()) {
            throw new IllegalStateException("This stand is already allocated");
        }

        User applicant = userRepository.findById(userId).orElseThrow();

        // Ensure same organization allocation
        if (stand.getOrganization() != null && applicant.getOrganization() != null
                && !stand.getOrganization().getId().equals(applicant.getOrganization().getId())) {
            throw new IllegalArgumentException("User and stand must belong to the same organization");
        }

        stand.setAllocated(true);
        stand.setAllocatedTo(applicant);
        stand.setAllocationDate(LocalDate.now());
        stand.setFeePaid(false);

        return landStandRepository.save(stand);
    }

    /**
     * Allow a regular user to apply for a stand. No allocation is performed here.
     * The Top 10 may later review and allocate the stand.
     */
    @Override
    public LandStand applyForStand(Long standId, Long userId) {
        LandStand stand = landStandRepository.findById(standId)
                .orElseThrow(() -> new EntityNotFoundException("Stand not found"));
        if (stand.isAllocated()) {
            throw new IllegalStateException("Stand already allocated");
        }
        User user = userRepository.findById(userId).orElseThrow();
        if (stand.getOrganization() != null && user.getOrganization() != null
                && !stand.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new IllegalArgumentException("User and stand must belong to the same organization");
        }
        stand.setApplicant(user);
        stand.setApplicationDate(LocalDate.now());
        return landStandRepository.save(stand);
    }

    /**
     * Council (Top 10) direct assignment of a stand to a beneficiary without prior application.
     * Technical note: We verify the acting user has a COUNCIL_MEMBER role.
     */
    @Override
    public LandStand assignStandByCouncil(Long standId, Long actingCouncilUserId, Long beneficiaryUserId) {
        User acting = userRepository.findById(actingCouncilUserId).orElseThrow();
        Role councilRole = roleRepository.findByName("COUNCIL_MEMBER")
                .orElseThrow(() -> new EntityNotFoundException("Role COUNCIL_MEMBER not found"));
        if (!acting.getRoles().contains(councilRole)) {
            throw new IllegalArgumentException("Only council members can assign stands directly");
        }
        // Must be within their organization
        LandStand stand = landStandRepository.findById(standId)
                .orElseThrow(() -> new EntityNotFoundException("Stand not found"));
        if (stand.isAllocated()) {
            throw new IllegalStateException("This stand is already allocated");
        }
        if (acting.getOrganization() != null && stand.getOrganization() != null
                && !acting.getOrganization().getId().equals(stand.getOrganization().getId())) {
            throw new IllegalArgumentException("Council member may only assign stands within their organization");
        }
        return allocateStand(standId, beneficiaryUserId);
    }

    /**
     * Mark the stand fee as paid (after allocation).
     */
    @Override
    public LandStand markStandFeePaid(Long standId) {
        LandStand stand = landStandRepository.findById(standId)
                .orElseThrow(() -> new EntityNotFoundException("Stand not found"));
        if (!stand.isAllocated()) {
            throw new IllegalStateException("Cannot pay for a stand that is not allocated");
        }
        stand.setFeePaid(true);
        return landStandRepository.save(stand);
    }

    // ----- Pagination APIs -----
    @Override
    public PageResponse<LandStand> findAll(Pageable pageable) {
        Page<LandStand> page = landStandRepository.findAll(pageable);
        return PageResponse.from(page);
    }

    @Override
    public PageResponse<LandStand> findByAllocated(boolean allocated, Pageable pageable) {
        Page<LandStand> page = landStandRepository.findByAllocated(allocated, pageable);
        return PageResponse.from(page);
    }

    @Override
    public PageResponse<LandStand> findByType(StandType type, Pageable pageable) {
        Page<LandStand> page = landStandRepository.findByType(type, pageable);
        return PageResponse.from(page);
    }

    @Override
    public PageResponse<LandStand> findByOrganization(Long organizationId, Pageable pageable) {
        Page<LandStand> page = landStandRepository.findByOrganizationId(organizationId, pageable);
        return PageResponse.from(page);
    }

    @Override
    public PageResponse<LandStand> search(Long organizationId, Boolean allocated, StandType type, Pageable pageable) {
        Page<LandStand> page;
        boolean hasOrg = organizationId != null;
        boolean hasAllocated = allocated != null;
        boolean hasType = type != null;

        if (hasOrg && hasAllocated && hasType) {
            page = landStandRepository.findByOrganizationIdAndAllocatedAndType(organizationId, allocated, type, pageable);
        } else if (hasOrg && hasAllocated) {
            page = landStandRepository.findByOrganizationIdAndAllocated(organizationId, allocated, pageable);
        } else if (hasOrg && hasType) {
            page = landStandRepository.findByOrganizationIdAndType(organizationId, type, pageable);
        } else if (hasOrg) {
            page = landStandRepository.findByOrganizationId(organizationId, pageable);
        } else if (hasAllocated && hasType) {
            page = landStandRepository.findByAllocatedAndType(allocated, type, pageable);
        } else if (hasAllocated) {
            page = landStandRepository.findByAllocated(allocated, pageable);
        } else if (hasType) {
            page = landStandRepository.findByType(type, pageable);
        } else {
            page = landStandRepository.findAll(pageable);
        }
        return PageResponse.from(page);
    }
}
