package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.Role;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.domain.enums.StandType;
import com.tbf.tcms.repository.LandStandRepository;
import com.tbf.tcms.repository.RoleRepository;
import com.tbf.tcms.repository.UserRepository;
import com.tbf.tcms.web.dto.PageResponse;
import jakarta.persistence.EntityNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LandStandServiceImplTest {

    @Mock
    private LandStandRepository landStandRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private LandStandServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private LandStand newStand(Long id, Long orgId, boolean allocated) {
        LandStand s = new LandStand();
        s.setAllocated(allocated);
        s.setId(id);
        s.setType(StandType.RESIDENTIAL);
        Organization org = new Organization();
        org.setId(orgId);
        s.setOrganization(org);
        return s;
    }

    private User newUser(Long id, Long orgId) {
        User u = new User();
        u.setId(id);
        Organization org = new Organization();
        org.setId(orgId);
        u.setOrganization(org);
        return u;
    }

    @Test
    void shouldAllocateStandWhenValid() {
        LandStand s = newStand(1L, 99L, false);
        User u = newUser(10L, 99L);
        when(landStandRepository.findById(1L)).thenReturn(Optional.of(s));
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(landStandRepository.save(any(LandStand.class))).thenAnswer(inv -> inv.getArgument(0));

        LandStand out = service.allocateStand(1L, 10L);

        assertThat(out.isAllocated()).isTrue();
        assertThat(out.getAllocatedTo()).isEqualTo(u);
        assertThat(out.getAllocationDate()).isNotNull();
        assertThat(out.isFeePaid()).isFalse();
    }

    @Test
    void shouldThrowWhenStandNotFoundOnAllocate() {
        when(landStandRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.allocateStand(1L, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Stand not found");
    }

    @Test
    void shouldThrowWhenAllocatingAlreadyAllocated() {
        LandStand s = newStand(1L, 99L, true);
        when(landStandRepository.findById(1L)).thenReturn(Optional.of(s));
        assertThatThrownBy(() -> service.allocateStand(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already allocated");
    }

    @Test
    void shouldThrowWhenUserAndStandDifferentOrgOnAllocate() {
        LandStand s = newStand(1L, 99L, false);
        User u = newUser(10L, 88L);
        when(landStandRepository.findById(1L)).thenReturn(Optional.of(s));
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        assertThatThrownBy(() -> service.allocateStand(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same organization");
    }

    @Test
    void shouldApplyForStandWhenValid() {
        LandStand s = newStand(1L, 99L, false);
        User u = newUser(10L, 99L);
        when(landStandRepository.findById(1L)).thenReturn(Optional.of(s));
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(landStandRepository.save(any(LandStand.class))).thenAnswer(inv -> inv.getArgument(0));

        LandStand out = service.applyForStand(1L, 10L);
        assertThat(out.getApplicant()).isEqualTo(u);
        assertThat(out.getApplicationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldThrowWhenApplyForAllocatedStand() {
        LandStand s = newStand(1L, 99L, true);
        when(landStandRepository.findById(1L)).thenReturn(Optional.of(s));
        assertThatThrownBy(() -> service.applyForStand(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allocated");
    }

    @Test
    void shouldAssignByCouncilWhenUserHasRoleAndSameOrg() {
        LandStand s = newStand(1L, 77L, false);
        User acting = newUser(2L, 77L);
        Role council = new Role();
        council.setId(5L);
        council.setName("COUNCIL_MEMBER");
        acting.addRole(council);
        User beneficiary = newUser(3L, 77L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(acting));
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(council));
        when(landStandRepository.findById(1L)).thenReturn(Optional.of(s));
        when(userRepository.findById(3L)).thenReturn(Optional.of(beneficiary));
        when(landStandRepository.save(any(LandStand.class))).thenAnswer(i -> i.getArgument(0));

        LandStand out = service.assignStandByCouncil(1L, 2L, 3L);
        assertThat(out.isAllocated()).isTrue();
        assertThat(out.getAllocatedTo()).isEqualTo(beneficiary);
    }

    @Test
    void shouldThrowWhenAssignByNonCouncilMember() {
        User acting = newUser(2L, 77L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(acting));
        when(roleRepository.findByName("COUNCIL_MEMBER")).thenReturn(Optional.of(new Role()));
        assertThatThrownBy(() -> service.assignStandByCouncil(1L, 2L, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("council members");
    }

    @Test
    void shouldMarkFeePaidOnlyIfAllocated() {
        LandStand s = newStand(1L, 99L, true);
        when(landStandRepository.findById(1L)).thenReturn(Optional.of(s));
        when(landStandRepository.save(any(LandStand.class))).thenAnswer(i -> i.getArgument(0));

        LandStand out = service.markStandFeePaid(1L);
        assertThat(out.isFeePaid()).isTrue();

        LandStand notAllocated = newStand(2L, 99L, false);
        when(landStandRepository.findById(2L)).thenReturn(Optional.of(notAllocated));
        assertThatThrownBy(() -> service.markStandFeePaid(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not allocated");
    }

    @Test
    void verifyPagingMethodsDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LandStand> page = new PageImpl<>(List.of(newStand(1L, 1L, false)));
        when(landStandRepository.findAll(pageable)).thenReturn(page);

        PageResponse<LandStand> resp = service.findAll(pageable);
        assertThat(resp.totalElements()).isEqualTo(1);
        verify(landStandRepository).findAll(pageable);
    }
}
