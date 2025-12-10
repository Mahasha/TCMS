package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import com.tbf.tcms.domain.enums.CaseStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "dispute_cases")
public class DisputeCase extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private LocalDate openedDate;
    private LocalDate closedDate;

    @Enumerated(EnumType.STRING)
    private CaseStatus status;

    private int noticesSent = 0;

    @ManyToOne
    private User accusedUser;
    // The person who opened/laid the complaint for this case
    @ManyToOne
    private User complainant;
    @ManyToOne
    private Organization organization;
    @ManyToMany
    private Set<User> adjudicators = new HashSet<>();

    // Optional defense statement provided by the accused to dispute the case
    private String defenseStatement;
    private LocalDate defenseDate;
}
