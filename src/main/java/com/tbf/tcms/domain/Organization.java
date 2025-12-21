package com.tbf.tcms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tbf.tcms.domain.base.AuditableBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
public class Organization extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String type;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Organization parent;

    @OneToMany(mappedBy = "organization")
    @JsonIgnore
    private List<User> users;

    public Organization(String name, String type, Organization parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
    }
}
