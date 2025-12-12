package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.repository.OrganizationRepository;
import com.tbf.tcms.web.dto.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("shouldReturnHierarchyWhenOrganizationExists")
    void shouldReturnHierarchyWhenOrganizationExists() {
        Organization root = new Organization("Root", "AUTHORITY", null);
        root.setId(1L);
        root.setUsers(new ArrayList<>()); // avoid Hibernate.initialize(null)

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(root));
        when(organizationRepository.findSubOrganizations(1L)).thenReturn(List.of());

        Organization result = service.getHierarchy(1L);

        assertThat(result).isSameAs(root);
        verify(organizationRepository).findById(1L);
        verify(organizationRepository).findSubOrganizations(1L);
    }

    @Test
    @DisplayName("shouldThrowEntityNotFoundWhenOrganizationMissing")
    void shouldThrowEntityNotFoundWhenOrganizationMissing() {
        when(organizationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHierarchy(404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Organization not found");
        verify(organizationRepository).findById(404L);
        verify(organizationRepository, never()).findSubOrganizations(anyLong());
    }

    @Test
    @DisplayName("shouldCreateOrganizationWithParentWhenParentIdProvided")
    void shouldCreateOrganizationWithParentWhenParentIdProvided() {
        Organization parent = new Organization("Parent", "AUTHORITY", null);
        parent.setId(10L);

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(inv -> {
                    Organization o = inv.getArgument(0);
                    o.setId(11L);
                    return o;
                });

        Organization created = service.createOrganization("Child", "VILLAGE", 10L);

        assertThat(created.getId()).isEqualTo(11L);
        assertThat(created.getParent()).isSameAs(parent);
        assertThat(created.getName()).isEqualTo("Child");
        verify(organizationRepository).findById(10L);
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    @DisplayName("shouldCreateOrganizationWithoutParentWhenParentIdNull")
    void shouldCreateOrganizationWithoutParentWhenParentIdNull() {
        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Organization created = service.createOrganization("Solo", "DISTRICT", null);

        assertThat(created.getParent()).isNull();
        assertThat(created.getName()).isEqualTo("Solo");
        verify(organizationRepository, never()).findById(anyLong());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    @DisplayName("shouldThrowWhenParentNotFoundOnCreate")
    void shouldThrowWhenParentNotFoundOnCreate() {
        when(organizationRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createOrganization("X", "Y", 77L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Parent organization not found: 77");
        verify(organizationRepository).findById(77L);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldWrapFindAllResultsInPageResponse")
    void shouldWrapFindAllResultsInPageResponse() {
        PageRequest pr = PageRequest.of(0, 2);
        Organization o1 = new Organization("A", "T", null);
        Organization o2 = new Organization("B", "T", null);
        Page<Organization> page = new PageImpl<>(List.of(o1, o2), pr, 5);
        when(organizationRepository.findAll(pr)).thenReturn(page);

        PageResponse<Organization> resp = service.findAll(pr);
        assertThat(resp.currentPage()).isEqualTo(0);
        assertThat(resp.size()).isEqualTo(2);
        assertThat(resp.totalElements()).isEqualTo(5);
        assertThat(resp.content()).containsExactly(o1, o2);
        verify(organizationRepository).findAll(pr);
    }
}
