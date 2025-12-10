package com.tbf.tcms.web;

import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/{orgId}/hierarchy")
    public Organization getHierarchy(@PathVariable Long orgId) {
        return organizationService.getHierarchy(orgId);
    }

    @PostMapping
    public ResponseEntity<Organization> create(@RequestParam String name,
                                               @RequestParam String type,
                                               @RequestParam(required = false) Long parentId) {
        Organization created = organizationService.createOrganization(name, type, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
