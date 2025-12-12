package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.Family;
import com.tbf.tcms.domain.Organization;
import com.tbf.tcms.domain.VillageEvent;
import com.tbf.tcms.domain.enums.EventStatus;
import com.tbf.tcms.domain.enums.EventType;
import com.tbf.tcms.repository.FamilyRepository;
import com.tbf.tcms.repository.OrganizationRepository;
import com.tbf.tcms.repository.VillageEventRepository;
import com.tbf.tcms.service.VillageEventService;
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
public class VillageEventServiceImpl implements VillageEventService {

    private final VillageEventRepository villageEventRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyRepository familyRepository;

    @Override
    @Transactional
    public VillageEvent createEvent(Long organizationId,
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
                                    boolean hasIdCopies) {

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Family not found: " + familyId));

        VillageEvent event = new VillageEvent();
        event.setOrganization(org);
        event.setFamily(family);
        event.setType(type);
        event.setName(name);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setLocation(location);
        event.setDeathCertUrl(deathCertUrl);
        event.setIdCopyUrl(idCopyUrl);
        event.setHasDeathCertificate(hasDeathCertificate);
        event.setHasIdCopies(hasIdCopies);

        // Apply business rules
        BigDecimal feeToApply = feeAmount;
        if (type == EventType.FUNERAL) {
            if (!hasDeathCertificate || !hasIdCopies) {
                throw new IllegalArgumentException("FUNERAL events require death certificate and ID copies");
            }
            feeToApply = BigDecimal.valueOf(50);
        } else if (type == EventType.PARTY) {
            feeToApply = (feeAmount == null) ? BigDecimal.ZERO : feeAmount;
        } else if (type == EventType.TRADITIONAL_CEREMONY) {
            feeToApply = (feeAmount == null) ? BigDecimal.ZERO : feeAmount;
        }
        event.setFeeAmount(feeToApply);
        event.setStatus(EventStatus.PENDING_APPROVAL);

        // Notify the Chief (log only for now)
        log.info("Notify Chief: New {} event '{}' requested by family {} in organization {}",
                type, name, familyId, organizationId);

        return villageEventRepository.save(event);
    }
}
