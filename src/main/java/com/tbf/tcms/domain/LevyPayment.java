package com.tbf.tcms.domain;

import com.tbf.tcms.domain.base.AuditableBase;
import com.tbf.tcms.domain.enums.LevyStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "levy_payments")
@Data
@NoArgsConstructor
public class LevyPayment extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "financial_year", nullable = false)
    private int financialYear;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevyStatus status = LevyStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;
}
