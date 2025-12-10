package com.tbf.tcms.service;

import com.tbf.tcms.domain.Role;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.domain.enums.CaseStatus;
import com.tbf.tcms.repository.DisputeCaseRepository;
import com.tbf.tcms.repository.RoleRepository;
import com.tbf.tcms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DisputeCaseRepository caseRepository;

    /**
     * Disqualify a leader or council member (e.g., imprisonment >12 months)
     */
    public User disqualifyUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        user.setDisqualified(true);
        user.setDisqualificationReason(reason);

        roleRepository.findByName("COUNCIL_MEMBER").ifPresent(councilRole -> user.getRoles().remove(councilRole));
        return userRepository.save(user);
    }

    /**
     * Appoint Top 10 council
     */
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

    private boolean hasOpenCase(User user) {
        return caseRepository.existsByAccusedUserAndStatusIn(user,
                List.of(CaseStatus.OPEN, CaseStatus.NOTICE_1_SENT, CaseStatus.NOTICE_2_SENT, CaseStatus.NOTICE_3_SENT));
    }

    /**
     * Define heir — validates succession rules
     */
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

        heir.setHeirTo(leader);
        return userRepository.save(heir);
    }
}
