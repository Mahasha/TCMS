package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "residents")
@Data
@NoArgsConstructor
public class Resident extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String idNumber;
    private String phoneNumber;
    private String email;

    @Column(name = "is_head_of_household", nullable = false)
    private boolean isHeadOfHousehold = false;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;
}
