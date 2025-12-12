package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.Family;
import com.tbf.tcms.domain.LevyPayment;
import com.tbf.tcms.domain.enums.LevyStatus;
import com.tbf.tcms.repository.FamilyRepository;
import com.tbf.tcms.repository.LevyPaymentRepository;
import com.tbf.tcms.service.LevyService;
import com.tbf.tcms.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LevyServiceImpl implements LevyService {

    private static final BigDecimal DEFAULT_ANNUAL_LEVY = new BigDecimal("100.00");

    private final LevyPaymentRepository levyPaymentRepository;
    private final FamilyRepository familyRepository;

    @Override
    @Transactional
    public LevyPayment recordPayment(Long familyId, BigDecimal amount, int year) {
        log.info("Recording levy payment for family {}: amount {} year {}", familyId, amount, year);
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Family not found with id: " + familyId));

        BigDecimal effectiveAmount = (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
                ? DEFAULT_ANNUAL_LEVY
                : amount;

        LevyPayment payment = levyPaymentRepository
                .findByFamilyIdAndFinancialYear(familyId, year)
                .orElseGet(LevyPayment::new);

        payment.setFamily(family);
        payment.setFinancialYear(year);
        payment.setAmount(effectiveAmount);
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(LevyStatus.PAID);

        LevyPayment saved = levyPaymentRepository.save(payment);
        log.info("Levy payment recorded id {} for family {} year {} amount {} status {}",
                saved.getId(), familyId, year, saved.getAmount(), saved.getStatus());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLevyUpToDate(Long familyId) {
        int currentYear = LocalDate.now().getYear();
        boolean result = levyPaymentRepository
                .findByFamilyIdAndFinancialYear(familyId, currentYear)
                .map(lp -> lp.getStatus() == LevyStatus.PAID)
                .orElse(false);
        log.info("Levy status for family {} in {}: upToDate={}", familyId, currentYear, result);
        return result;
    }
}
