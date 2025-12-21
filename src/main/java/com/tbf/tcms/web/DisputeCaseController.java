package com.tbf.tcms.web;

import com.tbf.tcms.domain.DisputeCase;
import com.tbf.tcms.service.DisputeCaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "Dispute Cases", description = "Endpoints for opening, filing, notifying, defending, assigning adjudicators and closing dispute cases")
public class DisputeCaseController {

    private final DisputeCaseService disputeCaseService;

    @PostMapping("/open")
    public ResponseEntity<DisputeCase> openCase(@RequestParam String description,
                                                @RequestParam Long accusedUserId,
                                                @RequestParam Long orgId) {
        DisputeCase created = disputeCaseService.openCase(description, accusedUserId, orgId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/file")
    public ResponseEntity<DisputeCase> fileCase(@RequestParam String description,
                                                @RequestParam Long complainantUserId,
                                                @RequestParam Long accusedUserId,
                                                @RequestParam Long orgId) {
        DisputeCase created = disputeCaseService.fileCase(description, complainantUserId, accusedUserId, orgId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{caseId}/notice")
    public DisputeCase sendNotice(@PathVariable Long caseId) {
        return disputeCaseService.sendNotice(caseId);
    }

    @PostMapping("/{caseId}/defense")
    public DisputeCase disputeCase(@PathVariable Long caseId,
                                   @RequestParam Long accusedUserId,
                                   @RequestParam String defenseStatement) {
        return disputeCaseService.disputeCase(caseId, accusedUserId, defenseStatement);
    }

    @PostMapping("/{caseId}/adjudicators")
    public DisputeCase assignAdjudicators(@PathVariable Long caseId,
                                          @RequestBody @Valid @NotEmpty(message = "adjudicatorIds cannot be empty") List<@Positive(message = "adjudicatorId must be positive") Long> adjudicatorIds) {
        return disputeCaseService.assignAdjudicators(caseId, adjudicatorIds);
    }

    @PostMapping("/{caseId}/close")
    public DisputeCase closeCase(@PathVariable Long caseId) {
        return disputeCaseService.closeCase(caseId);
    }
}
