package com.tbf.tcms.web;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.enums.StandType;
import com.tbf.tcms.service.LandStandService;
import com.tbf.tcms.web.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/stands", "/api/land-stands"})
@RequiredArgsConstructor
public class LandStandController {

    private final LandStandService landStandService;

    // Grid listing: e.g., "All residential stands not yet allocated" for a village (orgId)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<LandStand>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Boolean allocated,
            @RequestParam(required = false) StandType type,
            @PageableDefault(size = 50, sort = {"standNumber"}) Pageable pageable
    ) {
        PageResponse<LandStand> page = landStandService.search(orgId, allocated, type, pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.totalElements()))
                .body(page);
    }

    @PostMapping("/{standId}/allocate")
    @PreAuthorize("hasRole('ADMIN')")
    public LandStand allocate(@PathVariable Long standId, @RequestParam Long userId) {
        return landStandService.allocateStand(standId, userId);
    }

    @PostMapping("/{standId}/apply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LandStand> apply(@PathVariable Long standId, @RequestParam Long userId) {
        LandStand updated = landStandService.applyForStand(standId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    @PostMapping("/{standId}/assign-by-council")
    @PreAuthorize("hasRole('ADMIN')")
    public LandStand assignByCouncil(@PathVariable Long standId,
                                     @RequestParam Long actingCouncilUserId,
                                     @RequestParam Long beneficiaryUserId) {
        return landStandService.assignStandByCouncil(standId, actingCouncilUserId, beneficiaryUserId);
    }

    @PostMapping("/{standId}/fee/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public LandStand markFeePaid(@PathVariable Long standId) {
        return landStandService.markStandFeePaid(standId);
    }
}
