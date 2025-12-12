package com.tbf.tcms.service.impl;

import com.tbf.tcms.domain.Family;
import com.tbf.tcms.domain.Resident;
import com.tbf.tcms.repository.ResidentRepository;
import com.tbf.tcms.service.LevyService;
import com.tbf.tcms.service.ResidentService;
import com.tbf.tcms.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResidentServiceImpl implements ResidentService {

    private final ResidentRepository residentRepository;
    private final LevyService levyService;

    @Override
    @Transactional(readOnly = true)
    public String generateProofOfResidence(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId));

        Family family = resident.getFamily();
        if (family == null) {
            throw new ResourceNotFoundException("Resident does not belong to a family");
        }

        boolean upToDate = levyService.isLevyUpToDate(family.getId());
        if (!upToDate) {
            throw new IllegalStateException("Levy payments are in arrears.");
        }

        String name = String.format("%s %s", nullToEmpty(resident.getFirstName()), nullToEmpty(resident.getLastName())).trim();
        String address = nullToEmpty(family.getAddress());
        return String.format("This confirms that %s resides at %s and is in good standing.", name, address);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
