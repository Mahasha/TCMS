package com.tbf.tcms.web.dto.landstand;

import com.tbf.tcms.domain.enums.StandType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LandStandResponseDto {
    private Long id;
    private String standNumber;
    private StandType type;
    private boolean allocated;
    private boolean feePaid;
    private LocalDate allocationDate;
    private LocalDate applicationDate;
    private Long organizationId;
    private Long allocatedToUserId;
    private Long applicantUserId;
}
