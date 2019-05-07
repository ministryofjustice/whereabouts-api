package uk.gov.justice.digital.hmpps.whereabouts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "OFFENDER_ATTENDANCE")
@Data()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id()
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @NotNull
    private long offenderBookingId;

    @NotNull
    private long eventId;

    @NotNull
    private long eventLocationId;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TimePeriod period;

    @NotNull
    @Length(max = 6)
    private String prisonId;

    private boolean paid;
    private boolean attended;
    private int absentReasonId;
}
