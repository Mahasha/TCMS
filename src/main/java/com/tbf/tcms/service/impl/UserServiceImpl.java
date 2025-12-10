package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.Role;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.domain.enums.CaseStatus;
import com.tbf.tcms.repository.DisputeCaseRepository;
import com.tbf.tcms.repository.OrganizationRepository;
import com.tbf.tcms.repository.RoleRepository;
import com.tbf.tcms.repository.UserRepository;
import com.tbf.tcms.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final DisputeCaseRepository caseRepository;

    /**
     * Disqualify a leader or council member (e.g., imprisonment >12 months).
     * Technical note: this sets a flag and removes the council role if present. Controllers
     * should record the actor performing the disqualification for audit outside this method.
     */
    @Override
    public User disqualifyUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        user.setDisqualified(true);
        user.setDisqualificationReason(reason);

        roleRepository.findByName("COUNCIL_MEMBER").ifPresent(councilRole -> user.getRoles().remove(councilRole));
        return userRepository.save(user);
    }

    /**
     * Create a new user under an organization and optionally set base attributes.
     * - Ensures the organization exists.
     * - Initializes with no roles and not disqualified.
     */
    @Override
    public User createUser(String fullName, String lineage, Long organizationId, LocalDate birthDate) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + organizationId));
        User u = new User();
        u.setFullName(fullName);
        u.setLineage(lineage);
        u.setOrganization(org);
        u.setBirthDate(birthDate);
        u.setDisqualified(false);
        return userRepository.save(u);
    }

    /**
     * Assign a named role to a user (idempotent).
     * Technical note: This method only adds, never removes roles.
     */
    @Override
    public User assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
        user.addRole(role);
        return userRepository.save(user);
    }

    /**
     * Appoint Top 10 council.
     * Technical note: This computes and assigns the COUNCIL_MEMBER role to exactly 10 members
     * based on lineage and age, skipping users with open cases.
     */
    @Override
    public List<User> appointTopCouncil(Long orgId, int size) {
        if (size != 10) throw new IllegalArgumentException("Top Council must have exactly 10 members");

        Role councilRole = roleRepository.findByName("COUNCIL_MEMBER")
                .orElseThrow(() -> new EntityNotFoundException("Role COUNCIL_MEMBER not found"));

        // Get all eligible people in this village
        List<User> candidates = userRepository.findEligibleUsersByOrganization(orgId).stream()
                .filter(u -> u.getAge() >= 21)  // Must be an adult
                .filter(u -> !hasOpenCase(u))   // No active cases
                .sorted((a, b) -> {
                    // Priority: Family lineage first, then older age
                    if (!a.getLineage().equals(b.getLineage())) {
                        return a.getLineage().equals("FAMILY") ? -1 : 1;
                    }
                    return b.getAge() - a.getAge(); // older first
                })
                .limit(20) // just in case, take the best 20 candidates
                .toList();

        // Rule: 6 from family, 4 from community
        List<User> family = candidates.stream()
                .filter(u -> "FAMILY".equals(u.getLineage())).limit(6).toList();
        List<User> community = candidates.stream()
                .filter(u -> "COMMUNITY".equals(u.getLineage())).limit(4).toList();

        List<User> topCouncil = new ArrayList<>();
        topCouncil.addAll(family);
        topCouncil.addAll(community);

        if (topCouncil.size() < 10) {
            throw new IllegalStateException("Not enough eligible candidates to form Top 10 Council");
        }

        // Assign role
        topCouncil.forEach(member -> member.addRole(councilRole));
        userRepository.saveAll(topCouncil);

        return topCouncil;
    }

    /**
     * Appoint an individual user to the council (COUNCIL_MEMBER) if there is capacity in the Top 10.
     * Rules enforced:
     * - User must be an adult (>=21), not disqualified, and have no open case.
     * - Council per organization is capped at 10 members.
     */
    @Override
    public User appointUserToCouncil(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        if (user.isDisqualified()) {
            throw new IllegalStateException("Cannot appoint a disqualified user");
        }
        if (user.getAge() < 21) {
            throw new IllegalArgumentException("Council members must be at least 21 years old");
        }
        if (hasOpenCase(user)) {
            throw new IllegalStateException("User has an open case and cannot be appointed");
        }

        Role councilRole = roleRepository.findByName("COUNCIL_MEMBER")
                .orElseThrow(() -> new EntityNotFoundException("Role COUNCIL_MEMBER not found"));
        long currentCount = userRepository.countUsersWithRoleInOrganization(user.getOrganization().getId(), "COUNCIL_MEMBER");
        if (currentCount >= 10) {
            throw new IllegalStateException("Top 10 council is already full for this organization");
        }
        user.addRole(councilRole);
        return userRepository.save(user);
    }

    private boolean hasOpenCase(User user) {
        return caseRepository.existsByAccusedUserAndStatusIn(user,
                List.of(CaseStatus.OPEN, CaseStatus.NOTICE_1_SENT, CaseStatus.NOTICE_2_SENT, CaseStatus.NOTICE_3_SENT));
    }

    /**
     * Define heir — validates succession rules.
     * Only leaders with a role NTONA or CHIEF may designate an heir.
     */
    @Override
    public User defineHeir(Long leaderId, Long heirUserId) {
        User leader = userRepository.findById(leaderId).orElseThrow();
        User heir = userRepository.findById(heirUserId).orElseThrow();

        // Rules from the workshop
        if (!heir.getLineage().equals("FAMILY")) {
            throw new IllegalArgumentException("Heir must be from the family lineage");
        }
        if (heir.getAge() < 18) {
            throw new IllegalArgumentException("Heir must be at least 18 years old");
        }

        // Authority check: leader must have NTONA or CHIEF role
        boolean hasAuthority = roleRepository.findByName("NTONA").map(leader.getRoles()::contains).orElse(false)
                || roleRepository.findByName("CHIEF").map(leader.getRoles()::contains).orElse(false);
        if (!hasAuthority) {
            throw new IllegalArgumentException("Only Ntona or Chief can define an heir");
        }

        // Must be in the same organization hierarchy (basic same organization check here)
        if (leader.getOrganization() != null && heir.getOrganization() != null
                && !leader.getOrganization().getId().equals(heir.getOrganization().getId())) {
            throw new IllegalArgumentException("Leader and heir must belong to the same organization");
        }

        heir.setHeirTo(leader);
        return userRepository.save(heir);
    }
}
