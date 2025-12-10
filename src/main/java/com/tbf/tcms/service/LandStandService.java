package com.tbf.tcms.service;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.Role;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.repository.LandStandRepository;
import com.tbf.tcms.repository.RoleRepository;
import com.tbf.tcms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class LandStandService {

    private final LandStandRepository landStandRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Allocate a stand to a specific user.
     * Use this from administrative flows (Top 10/Council assignment) when bypassing the application queue.
     * Technical note: We intentionally keep this low-level and perform validations in caller methods.
     */
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
     * Technical note: We verify acting user has COUNCIL_MEMBER role.
     */
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
    public LandStand markStandFeePaid(Long standId) {
        LandStand stand = landStandRepository.findById(standId)
                .orElseThrow(() -> new EntityNotFoundException("Stand not found"));
        if (!stand.isAllocated()) {
            throw new IllegalStateException("Cannot pay for a stand that is not allocated");
        }
        stand.setFeePaid(true);
        return landStandRepository.save(stand);
    }
}
