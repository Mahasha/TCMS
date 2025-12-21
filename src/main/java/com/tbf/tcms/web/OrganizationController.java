package com.tbf.tcms.web;

import com.tbf.tcms.domain.DisputeCase;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.enums.CaseStatus;
import com.tbf.tcms.service.DisputeCaseService;
import com.tbf.tcms.service.OrganizationService;
import com.tbf.tcms.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Endpoints for managing organizations and viewing related cases")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final DisputeCaseService disputeCaseService;

    @GetMapping("/{orgId}/hierarchy")
    @PreAuthorize("hasRole('ADMIN')")
    public Organization getHierarchy(@PathVariable Long orgId) {
        return organizationService.getHierarchy(orgId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Organization> create(@RequestParam String name,
                                               @RequestParam String type,
                                               @RequestParam(required = false) Long parentId) {
        Organization created = organizationService.createOrganization(name, type, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Example: Ntona viewing all OPEN cases in the village (paged)
    @GetMapping("/{orgId}/cases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<DisputeCase>> listCasesForOrganization(
            @PathVariable Long orgId,
            @RequestParam(required = false) CaseStatus status,
            @PageableDefault(size = 10, sort = {"openedDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<DisputeCase> page = (status == null)
                ? disputeCaseService.findByOrganization(orgId, pageable)
                : disputeCaseService.findByOrganizationAndStatus(orgId, status, pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.totalElements()))
                .body(page);
    }
}
