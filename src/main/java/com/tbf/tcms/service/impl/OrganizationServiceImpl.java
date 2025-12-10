package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.repository.OrganizationRepository;
import com.tbf.tcms.service.OrganizationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository orgRepository;

    /**
     * Get full hierarchy under an organization (e.g., Main Authority → all villages)
     */
    @Override
    public Organization getHierarchy(Long orgId) {
        Organization root = orgRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        fetchChildrenRecursively(root);
        return root;
    }

    /**
     * Create a new organization under an optional parent.
     * Technical note: we only validate existence of parent when provided; name/type uniqueness is out of scope here.
     */
    @Override
    public Organization createOrganization(String name, String type, Long parentId) {
        Organization parent = null;
        if (parentId != null) {
            parent = orgRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent organization not found: " + parentId));
        }
        Organization org = new Organization(name, type, parent);
        return orgRepository.save(org);
    }

    private void fetchChildrenRecursively(Organization org) {
        List<Organization> children = orgRepository.findSubOrganizations(org.getId());
        // Load current org users to avoid LazyInitialization outside transaction
        Hibernate.initialize(org.getUsers());
        for (Organization child : children) {
            fetchChildrenRecursively(child);
        }
    }
}
