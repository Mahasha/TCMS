package com.tbf.tcms.repository;

import com.tbf.tcms.domain.LevyPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevyPaymentRepository extends JpaRepository<LevyPayment, Long> {
    Optional<LevyPayment> findByFamilyIdAndFinancialYear(Long familyId, int financialYear);
}
