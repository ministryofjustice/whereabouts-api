package uk.gov.justice.digital.hmpps.whereabouts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BookingActivity {
    private Long bookingId;
    private Long activityId;
}
