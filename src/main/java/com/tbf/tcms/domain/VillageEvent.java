package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import com.tbf.tcms.domain.enums.EventStatus;
import com.tbf.tcms.domain.enums.EventType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "village_events")
@Data
@NoArgsConstructor
public class VillageEvent extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate eventDate;

    private String location;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PENDING_APPROVAL;

    @Enumerated(EnumType.STRING)
    private EventType type;

    // Document placeholders (to be stored in S3 later)
    private String deathCertUrl;
    private String idCopyUrl;

    // Request-time flags for validation (especially for FUNERAL)
    private boolean hasDeathCertificate;
    private boolean hasIdCopies;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;
}
