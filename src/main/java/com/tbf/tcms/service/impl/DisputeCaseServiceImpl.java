package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.DisputeCase;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.Role;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.domain.enums.CaseStatus;
import com.tbf.tcms.repository.DisputeCaseRepository;
import com.tbf.tcms.repository.OrganizationRepository;
import com.tbf.tcms.repository.RoleRepository;
import com.tbf.tcms.repository.UserRepository;
import com.tbf.tcms.service.DisputeCaseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DisputeCaseServiceImpl implements DisputeCaseService {

    private final DisputeCaseRepository caseRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository orgRepository;
    private final RoleRepository roleRepository;

    /**
     * Open a new dispute case by a complainant against an accused in a specific organization.
     * Technical note: this method is intentionally separated from assignAdjudicators/closeCase to
     * keep the case lifecycle clear (open → notices/defense → adjudication → close).
     */
    @Override
    public DisputeCase openCase(String description, Long accusedUserId, Long orgId) {
        User accused = userRepository.findById(accusedUserId).orElseThrow();
        Organization org = orgRepository.findById(orgId).orElseThrow();

        DisputeCase newCase = new DisputeCase();
        newCase.setDescription(description);
        newCase.setAccusedUser(accused);
        newCase.setOrganization(org);
        newCase.setStatus(CaseStatus.OPEN);
        newCase.setNoticesSent(0);
        newCase.setOpenedDate(LocalDate.now());

        return caseRepository.save(newCase);
    }

    /**
     * File a case with an explicit complainant recorded.
     * Use this when a community member raises a case against another user.
     */
    @Override
    public DisputeCase fileCase(String description, Long complainantUserId, Long accusedUserId, Long orgId) {
        User complainant = userRepository.findById(complainantUserId).orElseThrow();
        DisputeCase c = openCase(description, accusedUserId, orgId);
        c.setComplainant(complainant);
        return caseRepository.save(c);
    }

    @Override
    public DisputeCase sendNotice(Long caseId) {
        DisputeCase c = caseRepository.findById(caseId).orElseThrow();

        c.setNoticesSent(c.getNoticesSent() + 1);

        c.setStatus(switch (c.getNoticesSent()) {
            case 1 -> CaseStatus.NOTICE_1_SENT;
            case 2 -> CaseStatus.NOTICE_2_SENT;
            case 3 -> CaseStatus.NOTICE_3_SENT;
            default -> CaseStatus.REFERRED; // after 3 → go to mosate-mogolo
        });

        return caseRepository.save(c);
    }

    /**
     * Submit a defense statement by the accused to dispute the allegations.
     * Technical note: we only allow the accused to file a defense; UI should pass the authenticated user's id.
     */
    @Override
    public DisputeCase disputeCase(Long caseId, Long accusedUserId, String defenseStatement) {
        DisputeCase c = caseRepository.findById(caseId).orElseThrow();
        if (c.getAccusedUser() == null || !c.getAccusedUser().getId().equals(accusedUserId)) {
            throw new IllegalArgumentException("Only the accused user can submit a defense for this case");
        }
        if (c.getStatus() == CaseStatus.CLOSED) {
            throw new IllegalStateException("Cannot dispute a closed case");
        }
        c.setDefenseStatement(defenseStatement);
        c.setDefenseDate(LocalDate.now());
        return caseRepository.save(c);
    }

    @Override
    public DisputeCase assignAdjudicators(Long caseId, List<Long> adjudicatorIds) {
        DisputeCase c = caseRepository.findById(caseId).orElseThrow();

        List<User> adjudicators = userRepository.findAllById(adjudicatorIds);
        if (adjudicators.size() != adjudicatorIds.size()) {
            throw new IllegalArgumentException("Some adjudicators not found");
        }

        // Must be from same org and have COUNCIL_MEMBER role
        Role councilRole = roleRepository.findByName("COUNCIL_MEMBER").orElseThrow();
        for (User u : adjudicators) {
            if (!u.getOrganization().equals(c.getOrganization())) {
                throw new IllegalArgumentException(u.getFullName() + " not in this village");
            }
            if (!u.getRoles().contains(councilRole)) {
                throw new IllegalArgumentException(u.getFullName() + " is not a council member");
            }
        }

        c.setAdjudicators(new HashSet<>(adjudicators));
        return caseRepository.save(c);
    }

    @Override
    public DisputeCase closeCase(Long caseId) {
        DisputeCase c = caseRepository.findById(caseId).orElseThrow();
        c.setStatus(CaseStatus.CLOSED);
        c.setClosedDate(LocalDate.now());
        return caseRepository.save(c);
    }
}
