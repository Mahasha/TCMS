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
import com.tbf.tcms.web.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DisputeCaseServiceImplTest {

    @Mock private DisputeCaseRepository caseRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository orgRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks private DisputeCaseServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private User user(long id) { User u = new User(); u.setId(id); return u; }
    private Organization org(long id) { Organization o = new Organization(); o.setId(id); return o; }

    @Test
    void shouldOpenCaseWithDefaults() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10)));
        when(orgRepository.findById(1L)).thenReturn(Optional.of(org(1)));
        when(caseRepository.save(any(DisputeCase.class))).thenAnswer(i -> i.getArgument(0));

        DisputeCase c = service.openCase("desc", 10L, 1L);
        assertThat(c.getStatus()).isEqualTo(CaseStatus.OPEN);
        assertThat(c.getOpenedDate()).isEqualTo(LocalDate.now());
        assertThat(c.getNoticesSent()).isZero();
        assertThat(c.getAccusedUser().getId()).isEqualTo(10L);
    }

    @Test
    void shouldFileCaseWithComplainant() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1))); // complainant
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2))); // accused via openCase
        when(orgRepository.findById(5L)).thenReturn(Optional.of(org(5)));
        when(caseRepository.save(any(DisputeCase.class))).thenAnswer(i -> i.getArgument(0));

        DisputeCase c = service.fileCase("desc", 1L, 2L, 5L);
        assertThat(c.getComplainant().getId()).isEqualTo(1L);
    }

    @Test
    void shouldSendNoticeAdvanceStatusCorrectly() {
        DisputeCase c = new DisputeCase();
        c.setNoticesSent(0);
        when(caseRepository.findById(7L)).thenReturn(Optional.of(c));
        when(caseRepository.save(any(DisputeCase.class))).thenAnswer(i -> i.getArgument(0));

        c = service.sendNotice(7L);
        assertThat(c.getNoticesSent()).isEqualTo(1);
        assertThat(c.getStatus()).isEqualTo(CaseStatus.NOTICE_1_SENT);

        when(caseRepository.findById(7L)).thenReturn(Optional.of(c));
        c = service.sendNotice(7L);
        assertThat(c.getNoticesSent()).isEqualTo(2);
        assertThat(c.getStatus()).isEqualTo(CaseStatus.NOTICE_2_SENT);
    }

    @Test
    void shouldDisputeCaseOnlyByAccused() {
        DisputeCase c = new DisputeCase();
        User accused = user(9);
        c.setAccusedUser(accused);
        c.setStatus(CaseStatus.OPEN);
        when(caseRepository.findById(3L)).thenReturn(Optional.of(c));
        when(caseRepository.save(any(DisputeCase.class))).thenAnswer(i -> i.getArgument(0));

        DisputeCase out = service.disputeCase(3L, 9L, "my defense");
        assertThat(out.getDefenseStatement()).isEqualTo("my defense");
        assertThat(out.getDefenseDate()).isEqualTo(LocalDate.now());

        when(caseRepository.findById(3L)).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.disputeCase(3L, 8L, "bad"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the accused user");
    }

    @Test
    void shouldNotDisputeClosedCase() {
        DisputeCase c = new DisputeCase();
        User accused = user(1);
        c.setAccusedUser(accused);
        c.setStatus(CaseStatus.CLOSED);
        when(caseRepository.findById(2L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.disputeCase(2L, 1L, "x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void shouldAssignAdjudicatorsWithValidRoleAndOrg() {
        DisputeCase c = new DisputeCase();
        Organization org = org(10);
        c.setOrganization(org);
        when(caseRepository.findById(4L)).thenReturn(Optional.of(c));

        Role council = new Role(); council.setName("COUNCIL_MEMBER");
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(council));

        User u1 = user(1); u1.setOrganization(org);
        u1.addRole(council);
        User u2 = user(2); u2.setOrganization(org);
        u2.addRole(council);
        when(userRepository.findAllById(List.of(1L,2L))).thenReturn(List.of(u1,u2));
        when(caseRepository.save(any(DisputeCase.class))).thenAnswer(i -> i.getArgument(0));

        DisputeCase out = service.assignAdjudicators(4L, List.of(1L,2L));
        assertThat(out.getAdjudicators()).hasSize(2);
    }

    @Test
    void shouldCloseCaseSetsClosedFields() {
        DisputeCase c = new DisputeCase();
        c.setStatus(CaseStatus.OPEN);
        when(caseRepository.findById(8L)).thenReturn(Optional.of(c));
        when(caseRepository.save(any(DisputeCase.class))).thenAnswer(i -> i.getArgument(0));

        DisputeCase out = service.closeCase(8L);
        assertThat(out.getStatus()).isEqualTo(CaseStatus.CLOSED);
        assertThat(out.getClosedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void verifyPagingMethodsDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<DisputeCase> page = new PageImpl<>(List.of(new DisputeCase()));
        when(caseRepository.findAll(pageable)).thenReturn(page);

        PageResponse<DisputeCase> resp = service.findAll(pageable);
        assertThat(resp.totalElements()).isEqualTo(1);
        verify(caseRepository).findAll(pageable);
    }
}
