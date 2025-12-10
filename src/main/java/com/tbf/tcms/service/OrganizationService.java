package com.tbf.tcms.service;

import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository orgRepository;

    /**
     * Get full hierarchy under an organization (e.g., Main Authority → all villages)
     */
    public Organization getHierarchy(Long orgId) {
        Organization root = orgRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        fetchChildrenRecursively(root);
        return root;
    }

    private void fetchChildrenRecursively(Organization org) {
        List<Organization> children = orgRepository.findSubOrganizations(org.getId());
        Hibernate.initialize(org.getUsers());
        for (Organization child : children) {
            fetchChildrenRecursively(child);
        }
    }
}
