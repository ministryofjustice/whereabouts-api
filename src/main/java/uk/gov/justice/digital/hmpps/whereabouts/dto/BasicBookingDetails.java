package uk.gov.justice.digital.hmpps.whereabouts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


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
    public String offenderNo;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String agencyId;
    @NotNull
    private LocalDate dateOfBirth;
    private Long assignedLivingUnitId;
    public String assignedLivingUnitDesc;

}

