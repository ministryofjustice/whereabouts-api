package uk.gov.justice.digital.hmpps.whereabouts.dto;

import java.time.LocalDate;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BasicBookingDetails {

    @NotNull
    private Long bookingId;

    @NotBlank
    private String bookingNo;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String agencyId;

    private Long assignedLivingUnitId;

    @NotNull
    private boolean activeFlag;

    private Long facialImageId;

    @NotNull
    private LocalDate dateOfBirth;

    private Long assignedOfficerId;
}

