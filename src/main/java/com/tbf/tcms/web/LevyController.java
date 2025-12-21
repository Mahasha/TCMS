package com.tbf.tcms.web;

import com.tbf.tcms.domain.LevyPayment;
import com.tbf.tcms.service.LevyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/levies")
@RequiredArgsConstructor
@Tag(name = "Levies", description = "Levy payments and status endpoints")
public class LevyController {

    private final LevyService levyService;

    @PostMapping("/{familyId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<LevyPayment> recordPayment(@PathVariable Long familyId,
                                                     @RequestBody @Valid RecordPaymentRequest request) {
        int year = (request.getYear() != null) ? request.getYear() : LocalDate.now().getYear();
        LevyPayment saved = levyService.recordPayment(familyId, request.getAmount(), year);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{familyId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK','USER')")
    public LevyStatusResponse isUpToDate(@PathVariable Long familyId) {
        boolean upToDate = levyService.isLevyUpToDate(familyId);
        LevyStatusResponse resp = new LevyStatusResponse();
        resp.setYear(LocalDate.now().getYear());
        resp.setUpToDate(upToDate);
        return resp;
    }

    @Data
    public static class RecordPaymentRequest {
        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than 0")
        private BigDecimal amount; // required and > 0

        @Positive(message = "year must be a positive number")
        private Integer year;      // optional; defaults to current year
    }

    @Data
    public static class LevyStatusResponse {
        private int year;
        private boolean upToDate;
    }
}
