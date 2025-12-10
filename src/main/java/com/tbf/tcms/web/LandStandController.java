package com.tbf.tcms.web;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.service.LandStandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stands")
@RequiredArgsConstructor
public class LandStandController {

    private final LandStandService landStandService;

    @PostMapping("/{standId}/allocate")
    public LandStand allocate(@PathVariable Long standId, @RequestParam Long userId) {
        return landStandService.allocateStand(standId, userId);
    }

    @PostMapping("/{standId}/apply")
    public ResponseEntity<LandStand> apply(@PathVariable Long standId, @RequestParam Long userId) {
        LandStand updated = landStandService.applyForStand(standId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    @PostMapping("/{standId}/assign-by-council")
    public LandStand assignByCouncil(@PathVariable Long standId,
                                     @RequestParam Long actingCouncilUserId,
                                     @RequestParam Long beneficiaryUserId) {
        return landStandService.assignStandByCouncil(standId, actingCouncilUserId, beneficiaryUserId);
    }

    @PostMapping("/{standId}/fee/mark-paid")
    public LandStand markFeePaid(@PathVariable Long standId) {
        return landStandService.markStandFeePaid(standId);
    }
}
