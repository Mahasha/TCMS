package com.tbf.tcms.service;

import com.tbf.tcms.domain.VillageEvent;
import com.tbf.tcms.domain.enums.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface VillageEventService {

    VillageEvent createEvent(Long organizationId,
                             Long familyId,
                             EventType type,
                             String name,
                             String description,
                             LocalDate eventDate,
                             String location,
                             BigDecimal feeAmount,
                             String deathCertUrl,
                             String idCopyUrl,
                             boolean hasDeathCertificate,
                             boolean hasIdCopies);
}
