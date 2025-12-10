package com.tbf.tcms.service;

import com.tbf.tcms.domain.LandStand;
import com.tbf.tcms.domain.User;
import com.tbf.tcms.repository.LandStandRepository;
import com.tbf.tcms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class LandStandService {

    private final LandStandRepository landStandRepository;
    private final UserRepository userRepository;

    public LandStand allocateStand(Long standId, Long userId) {
        LandStand stand = landStandRepository.findById(standId)
                .orElseThrow(() -> new EntityNotFoundException("Stand not found"));

        if (stand.isAllocated()) {
            throw new IllegalStateException("This stand is already allocated");
        }

        User applicant = userRepository.findById(userId).orElseThrow();

        stand.setAllocated(true);
        stand.setAllocatedTo(applicant);
        stand.setAllocationDate(LocalDate.now());
        stand.setFeePaid(false);

        return landStandRepository.save(stand);
    }
}
