package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
