package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import com.tbf.tcms.domain.enums.StandType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "land_stands")
public class LandStand extends AuditableBase {

    @Id
    @GeneratedValue
    private Long id;
    private String standNumber;
    @Enumerated(EnumType.STRING)
    private StandType type;
    private double sizeInSquareMeters;
    private boolean allocated = false;
    private LocalDate allocationDate;
    @Column(name = "fee_paid", nullable = false)
    private boolean feePaid = false;

    @ManyToOne
    private User allocatedTo;
    // Applicant who submitted interest for this stand (before allocation)
    @ManyToOne
    private User applicant;
    private LocalDate applicationDate;
    @ManyToOne
    private Organization organization;
}
