package uk.gov.justice.digital.hmpps.prisonstaffhub.model;

import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "OFFENDER_EVENT")
@Data()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffenderEvent {

    @Id()
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "OFFENDER_EVENT_ID", nullable = false)
    private Long id;

    @NotNull
    @Length(max = 10)
    @Column(name = "OFFENDER_NO", nullable = false)
    private String offenderNo;

    @NotNull
    @Column(name = "EVENT_ID", nullable = false)
    private Long eventId;  //identifier returned for ACT/APP/VISIT  -  needs adding to elite2api (only exists for activity)

    @Column(name = "EVENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDate eventDate;

    @Column(name = "PERIOD", nullable = false)
    @Enumerated(EnumType.STRING)
    private TimePeriod period;

    @Column(name = "PRISON_ID", nullable = false)
    private String prisonId;

    @Column(name = "CURRENT_LOCATION", nullable = false)
    @Type(type = "yes_no")
    private Boolean currentLocation;
}
