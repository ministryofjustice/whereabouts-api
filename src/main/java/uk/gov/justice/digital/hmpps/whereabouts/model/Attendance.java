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

    @Enumerated(EnumType.STRING)
    private AbsentReason absentReason;

    @Length(max = 500)
    private String comments;

    private long caseNoteId;

    @CreatedDate
    @Column(name = "CREATE_DATETIME", nullable = false)
    private LocalDateTime creationDateTime;

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
