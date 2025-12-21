package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String lineage;

    private LocalDate birthDate;

    private boolean disqualified;

    private String disqualificationReason;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "heir_to_id")
    private User heirTo;

    public User(String fullName, String lineage, Organization organization) {
        this.fullName = fullName;
        this.lineage = lineage;
        this.organization = organization;
        this.disqualified = false;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public int getAge() {
        return Period.between(this.birthDate, LocalDate.now()).getYears();
    }
}