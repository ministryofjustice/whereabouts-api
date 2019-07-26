package uk.gov.justice.digital.hmpps.whereabouts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "OFFENDER_ATTENDANCE")
@Data()
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder(toBuilder = true)
public class Attendance {
    @Id()
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long bookingId;

    @NotNull
    private Long eventId;

    @NotNull
    private Long eventLocationId;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TimePeriod period;

    @NotNull
    @Length(max = 6)
    private String prisonId;

    private Boolean paid;
    private Boolean attended;

    @Enumerated(EnumType.STRING)
    private AbsentReason absentReason;

    @Length(max = 240)
    private String comments;

    private Long caseNoteId;

    @CreatedDate
    @Column(name = "CREATE_DATETIME", nullable = false)
    private LocalDateTime createDateTime;

    @CreatedBy
    @Column(name = "CREATE_USER_ID", nullable = false)
    private String createUserId;

    @LastModifiedDate
    @Column(name = "MODIFY_DATETIME")
    private LocalDateTime modifyDateTime;

    @LastModifiedBy
    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;
}
