package com.tbf.tcms.service;

import com.tbf.tcms.domain.LevyPayment;

import java.math.BigDecimal;

public interface LevyService {

    LevyPayment recordPayment(Long familyId, BigDecimal amount, int year);

    boolean isLevyUpToDate(Long familyId);
}
