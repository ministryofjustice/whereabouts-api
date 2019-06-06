package uk.gov.justice.digital.hmpps.whereabouts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BasicBookingDetails {

    private Long bookingId;
    private String bookingNo;
    private String offenderNo;
    private String firstName;
    private String middleName;
    private String lastName;
    private String agencyId;
    private Long assignedLivingUnitId;
    private boolean activeFlag;
    private Long facialImageId;
    private LocalDate dateOfBirth;
    private Long assignedOfficerId;
}

