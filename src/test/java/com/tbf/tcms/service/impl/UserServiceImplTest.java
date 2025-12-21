package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.Role;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.repository.DisputeCaseRepository;
import com.tbf.tcms.repository.OrganizationRepository;
import com.tbf.tcms.repository.RoleRepository;
import com.tbf.tcms.repository.UserRepository;
import com.tbf.tcms.web.dto.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private DisputeCaseRepository caseRepository;

    @InjectMocks private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helpers
    private User newUser(long id, String name, String lineage, Organization org, int ageYears) {
        User u = new User();
        u.setId(id);
        u.setFullName(name);
        u.setLineage(lineage);
        u.setOrganization(org);
        u.setBirthDate(LocalDate.now().minusYears(ageYears));
        u.setRoles(new HashSet<>());
        u.setDisqualified(false);
        return u;
    }

    private Role role(String name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }

    @Test
    @DisplayName("shouldDisqualifyUserAndRemoveCouncilRole")
    void shouldDisqualifyUserAndRemoveCouncilRole() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(5L, "Alice", "FAMILY", org, 30);
        Role council = role("COUNCIL_MEMBER");
        user.getRoles().add(council);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(council));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.disqualifyUser(5L, "Imprisoned");

        assertThat(result.isDisqualified()).isTrue();
        assertThat(result.getDisqualificationReason()).isEqualTo("Imprisoned");
        assertThat(result.getRoles()).doesNotContain(council);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("shouldCreateUserWhenOrganizationExists")
    void shouldCreateUserWhenOrganizationExists() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(10L);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = service.createUser("Bob", "COMMUNITY", 10L, LocalDate.of(1990,1,1));
        assertThat(created.getOrganization()).isSameAs(org);
        assertThat(created.getFullName()).isEqualTo("Bob");
        assertThat(created.isDisqualified()).isFalse();
    }

    @Test
    @DisplayName("shouldThrowWhenOrganizationMissingOnCreateUser")
    void shouldThrowWhenOrganizationMissingOnCreateUser() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createUser("Bob", "COMMUNITY", 99L, LocalDate.now()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Organization not found: 99");
    }

    @Test
    @DisplayName("shouldAssignRoleToUserWhenBothExist")
    void shouldAssignRoleToUserWhenBothExist() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(7L, "Eve", "FAMILY", org, 25);
        Role r = role("NTONA");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("NTONA")).thenReturn(Optional.of(r));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User out = service.assignRoleToUser(7L, "NTONA");
        assertThat(out.getRoles()).contains(r);
    }

    @Test
    @DisplayName("shouldThrowWhenAssignRoleUserNotFound")
    void shouldThrowWhenAssignRoleUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.assignRoleToUser(1L, "X"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: 1");
    }

    @Test
    @DisplayName("shouldThrowWhenAssignRoleRoleNotFound")
    void shouldThrowWhenAssignRoleRoleNotFound() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(7L, "Eve", "FAMILY", org, 25);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRoleToUser(7L, "X"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role not found: X");
    }

    @Test
    @DisplayName("shouldAppointTopCouncilWithExactTenMembers")
    void shouldAppointTopCouncilWithExactTenMembers() {
        Role council = role("COUNCIL_MEMBER");
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(council));

        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        List<User> candidates = new ArrayList<>();
        // 6 FAMILY + 4 COMMUNITY, all adults
        for (int i = 0; i < 6; i++) candidates.add(newUser(i+1, "F"+i, "FAMILY", org, 30 - i));
        for (int i = 0; i < 4; i++) candidates.add(newUser(i+10, "C"+i, "COMMUNITY", org, 28 - i));

        when(userRepository.findEligibleUsersByOrganization(1L)).thenReturn(candidates);
        when(userRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(caseRepository.existsByAccusedUserAndStatusIn(any(), anyList())).thenReturn(false);

        List<User> appointed = service.appointTopCouncil(1L, 10);
        assertThat(appointed).hasSize(10);
        assertThat(appointed).allMatch(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("COUNCIL_MEMBER")));
        verify(userRepository).saveAll(appointed);
    }

    @Test
    @DisplayName("shouldThrowWhenTopCouncilSizeIsNotTen")
    void shouldThrowWhenTopCouncilSizeIsNotTen() {
        assertThatThrownBy(() -> service.appointTopCouncil(1L, 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly 10");
    }

    @Test
    @DisplayName("shouldThrowWhenNotEnoughEligibleCandidatesForTopCouncil")
    void shouldThrowWhenNotEnoughEligibleCandidatesForTopCouncil() {
        Role council = role("COUNCIL_MEMBER");
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(council));
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        // only 9 eligible
        List<User> candidates = new ArrayList<>();
        for (int i = 0; i < 9; i++) candidates.add(newUser(i+1, "U"+i, i<6?"FAMILY":"COMMUNITY", org, 30));
        when(userRepository.findEligibleUsersByOrganization(1L)).thenReturn(candidates);
        when(caseRepository.existsByAccusedUserAndStatusIn(any(), anyList())).thenReturn(false);

        assertThatThrownBy(() -> service.appointTopCouncil(1L, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough eligible candidates");
    }

    @Test
    @DisplayName("shouldAppointUserToCouncilHappyPath")
    void shouldAppointUserToCouncilHappyPath() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(1L, "Tom", "FAMILY", org, 25);
        Role council = role("COUNCIL_MEMBER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(council));
        when(userRepository.countUsersWithRoleInOrganization(1L, "COUNCIL_MEMBER")).thenReturn(5L);
        when(caseRepository.existsByAccusedUserAndStatusIn(eq(user), anyList())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = service.appointUserToCouncil(1L);
        assertThat(saved.getRoles()).extracting(Role::getName).contains("COUNCIL_MEMBER");
    }

    @Test
    @DisplayName("shouldNotAppointDisqualifiedUserToCouncil")
    void shouldNotAppointDisqualifiedUserToCouncil() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(1L, "Tom", "FAMILY", org, 25);
        user.setDisqualified(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.appointUserToCouncil(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disqualified");
    }

    @Test
    @DisplayName("shouldNotAppointUnderageUserToCouncil")
    void shouldNotAppointUnderageUserToCouncil() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(1L, "Tim", "FAMILY", org, 20);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.appointUserToCouncil(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 21");
    }

    @Test
    @DisplayName("shouldNotAppointUserWithOpenCaseToCouncil")
    void shouldNotAppointUserWithOpenCaseToCouncil() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(1L, "Tim", "FAMILY", org, 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(caseRepository.existsByAccusedUserAndStatusIn(eq(user), anyList())).thenReturn(true);

        assertThatThrownBy(() -> service.appointUserToCouncil(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("open case");
    }

    @Test
    @DisplayName("shouldNotAppointWhenCouncilIsFull")
    void shouldNotAppointWhenCouncilIsFull() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User user = newUser(1L, "Tom", "FAMILY", org, 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(caseRepository.existsByAccusedUserAndStatusIn(eq(user), anyList())).thenReturn(false);
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(role("COUNCIL_MEMBER")));
        when(userRepository.countUsersWithRoleInOrganization(1L, "COUNCIL_MEMBER")).thenReturn(10L);

        assertThatThrownBy(() -> service.appointUserToCouncil(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already full");
    }

    @Test
    @DisplayName("shouldDefineHeirWhenRulesSatisfied")
    void shouldDefineHeirWhenRulesSatisfied() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User leader = newUser(1L, "Leader", "FAMILY", org, 50);
        User heir = newUser(2L, "Heir", "FAMILY", org, 20);
        Role ntona = role("NTONA");
        leader.getRoles().add(ntona);

        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(userRepository.findById(2L)).thenReturn(Optional.of(heir));
        when(roleRepository.findByName("NTONA")).thenReturn(Optional.of(ntona));
        when(roleRepository.findByName("CHIEF")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.defineHeir(1L, 2L);
        assertThat(result.getHeirTo()).isEqualTo(leader);
    }

    @Test
    @DisplayName("shouldNotDefineHeirWhenHeirNotFamily")
    void shouldNotDefineHeirWhenHeirNotFamily() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User leader = newUser(1L, "Leader", "FAMILY", org, 50);
        User heir = newUser(2L, "Heir", "COMMUNITY", org, 20);
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(userRepository.findById(2L)).thenReturn(Optional.of(heir));

        assertThatThrownBy(() -> service.defineHeir(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("family lineage");
    }

    @Test
    @DisplayName("shouldNotDefineHeirWhenUnder18")
    void shouldNotDefineHeirWhenUnder18() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User leader = newUser(1L, "Leader", "FAMILY", org, 50);
        User heir = newUser(2L, "Heir", "FAMILY", org, 17);
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(userRepository.findById(2L)).thenReturn(Optional.of(heir));

        assertThatThrownBy(() -> service.defineHeir(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 18");
    }

    @Test
    @DisplayName("shouldNotDefineHeirWhenLeaderLacksAuthority")
    void shouldNotDefineHeirWhenLeaderLacksAuthority() {
        Organization org = new Organization("Org", "VILLAGE", null);
        org.setId(1L);
        User leader = newUser(1L, "Leader", "FAMILY", org, 50);
        User heir = newUser(2L, "Heir", "FAMILY", org, 20);
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(userRepository.findById(2L)).thenReturn(Optional.of(heir));
        when(roleRepository.findByName("NTONA")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CHIEF")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.defineHeir(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only Ntona or Chief");
    }

    @Test
    @DisplayName("shouldNotDefineHeirWhenDifferentOrganizations")
    void shouldNotDefineHeirWhenDifferentOrganizations() {
        Organization org1 = new Organization("Org1", "VILLAGE", null); org1.setId(1L);
        Organization org2 = new Organization("Org2", "VILLAGE", null); org2.setId(2L);
        User leader = newUser(1L, "Leader", "FAMILY", org1, 50);
        User heir = newUser(2L, "Heir", "FAMILY", org2, 20);
        Role chief = role("CHIEF"); leader.getRoles().add(chief);
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(userRepository.findById(2L)).thenReturn(Optional.of(heir));
        when(roleRepository.findByName("NTONA")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CHIEF")).thenReturn(Optional.of(chief));

        assertThatThrownBy(() -> service.defineHeir(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("belong to the same organization");
    }

    @Test
    @DisplayName("shouldPageUsersFindAll")
    void shouldPageUsersFindAll() {
        PageRequest pr = PageRequest.of(0, 2);
        User u1 = new User();
        User u2 = new User();
        Page<User> page = new PageImpl<>(List.of(u1, u2), pr, 7);
        when(userRepository.findAll(pr)).thenReturn(page);

        PageResponse<User> resp = service.findAll(pr);
        assertThat(resp.totalElements()).isEqualTo(7);
        assertThat(resp.content()).containsExactly(u1, u2);
    }

    @Test
    @DisplayName("shouldPageUsersByOrganization")
    void shouldPageUsersByOrganization() {
        PageRequest pr = PageRequest.of(1, 5);
        User u = new User();
        Page<User> page = new PageImpl<>(List.of(u), pr, 6);
        when(userRepository.findByOrganizationId(9L, pr)).thenReturn(page);

        PageResponse<User> resp = service.findByOrganization(9L, pr);
        assertThat(resp.currentPage()).isEqualTo(1);
        assertThat(resp.totalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("shouldPageEligibleCouncilByOrganization")
    void shouldPageEligibleCouncilByOrganization() {
        PageRequest pr = PageRequest.of(0, 3);
        Page<User> page = new PageImpl<>(List.of(new User()), pr, 3);
        when(userRepository.findEligibleUsersByOrganization(3L, pr)).thenReturn(page);

        PageResponse<User> resp = service.findEligibleCouncilByOrganization(3L, pr);
        assertThat(resp.size()).isEqualTo(3);
    }
}
