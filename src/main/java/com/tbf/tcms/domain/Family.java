package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "families")
@Data
@NoArgsConstructor
public class Family extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Also referred to as stand number
    @Column(name = "reference_number")
    private String referenceNumber;

    private String address;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "family")
    private List<Resident> residents;
}
