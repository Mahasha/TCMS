package com.tbf.tcms.web;

import com.tbf.tcms.domain.VillageEvent;
import com.tbf.tcms.domain.enums.EventType;
import com.tbf.tcms.service.VillageEventService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/village-events")
@RequiredArgsConstructor
@Tag(name = "Village Events", description = "Create and manage village events")
public class VillageEventController {

    private final VillageEventService villageEventService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<VillageEvent> create(@RequestBody @Valid CreateEventRequest request) {
        VillageEvent created = villageEventService.createEvent(
                request.getOrganizationId(),
                request.getFamilyId(),
                request.getType(),
                request.getName(),
                request.getDescription(),
                request.getEventDate(),
                request.getLocation(),
                request.getFeeAmount(),
                request.getDeathCertUrl(),
                request.getIdCopyUrl(),
                Boolean.TRUE.equals(request.getHasDeathCertificate()),
                Boolean.TRUE.equals(request.getHasIdCopies())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Data
    public static class CreateEventRequest {
        @NotNull(message = "organizationId is required")
        @Positive(message = "organizationId must be positive")
        private Long organizationId;

        @NotNull(message = "familyId is required")
        @Positive(message = "familyId must be positive")
        private Long familyId;

        @NotNull(message = "type is required")
        private EventType type;

        @NotBlank(message = "name is required")
        private String name;

        private String description;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @NotNull(message = "eventDate is required")
        private LocalDate eventDate;

        @NotBlank(message = "location is required")
        private String location;

        private BigDecimal feeAmount; // optional; rules apply per type
        private String deathCertUrl;  // optional
        private String idCopyUrl;     // optional
        @NotNull(message = "hasDeathCertificate is required")
        private Boolean hasDeathCertificate; // required true for FUNERAL
        @NotNull(message = "hasIdCopies is required")
        private Boolean hasIdCopies;         // required true for FUNERAL
    }
}
