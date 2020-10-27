package uk.gov.justice.digital.hmpps.whereabouts.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "OFFENDER_ATTENDANCE")
@EntityListeners(AuditingEntityListener.class)
public class Attendance {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Size(max = 6)
    private String prisonId;

    private Boolean paid;
    private Boolean attended;

    @Enumerated(EnumType.STRING)
    private AbsentReason absentReason;

    @Size(max = 240)
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

    public Attendance(Long id, @NotNull Long bookingId, @NotNull Long eventId, @NotNull Long eventLocationId, @NotNull LocalDate eventDate, @NotNull TimePeriod period, @NotNull @Size(max = 6) String prisonId, Boolean paid, Boolean attended, AbsentReason absentReason, @Size(max = 240) String comments, Long caseNoteId, LocalDateTime createDateTime, String createUserId, LocalDateTime modifyDateTime, String modifyUserId) {
        this.id = id;
        this.bookingId = bookingId;
        this.eventId = eventId;
        this.eventLocationId = eventLocationId;
        this.eventDate = eventDate;
        this.period = period;
        this.prisonId = prisonId;
        this.paid = paid;
        this.attended = attended;
        this.absentReason = absentReason;
        this.comments = comments;
        this.caseNoteId = caseNoteId;
        this.createDateTime = createDateTime;
        this.createUserId = createUserId;
        this.modifyDateTime = modifyDateTime;
        this.modifyUserId = modifyUserId;
    }

    public Attendance() {
    }

    public static AttendanceBuilder builder() {
        return new AttendanceBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull Long getBookingId() {
        return this.bookingId;
    }

    public void setBookingId(@NotNull Long bookingId) {
        this.bookingId = bookingId;
    }

    public @NotNull Long getEventId() {
        return this.eventId;
    }

    public void setEventId(@NotNull Long eventId) {
        this.eventId = eventId;
    }

    public @NotNull Long getEventLocationId() {
        return this.eventLocationId;
    }

    public void setEventLocationId(@NotNull Long eventLocationId) {
        this.eventLocationId = eventLocationId;
    }

    public @NotNull LocalDate getEventDate() {
        return this.eventDate;
    }

    public void setEventDate(@NotNull LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public @NotNull TimePeriod getPeriod() {
        return this.period;
    }

    public void setPeriod(@NotNull TimePeriod period) {
        this.period = period;
    }

    public @NotNull @Size(max = 6) String getPrisonId() {
        return this.prisonId;
    }

    public void setPrisonId(@NotNull @Size(max = 6) String prisonId) {
        this.prisonId = prisonId;
    }

    public Boolean getPaid() {
        return this.paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Boolean getAttended() {
        return this.attended;
    }

    public void setAttended(Boolean attended) {
        this.attended = attended;
    }

    public AbsentReason getAbsentReason() {
        return this.absentReason;
    }

    public void setAbsentReason(AbsentReason absentReason) {
        this.absentReason = absentReason;
    }

    public @Size(max = 240) String getComments() {
        return this.comments;
    }

    public void setComments(@Size(max = 240) String comments) {
        this.comments = comments;
    }

    public Long getCaseNoteId() {
        return this.caseNoteId;
    }

    public void setCaseNoteId(Long caseNoteId) {
        this.caseNoteId = caseNoteId;
    }

    public LocalDateTime getCreateDateTime() {
        return this.createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getCreateUserId() {
        return this.createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public LocalDateTime getModifyDateTime() {
        return this.modifyDateTime;
    }

    public void setModifyDateTime(LocalDateTime modifyDateTime) {
        this.modifyDateTime = modifyDateTime;
    }

    public String getModifyUserId() {
        return this.modifyUserId;
    }

    public void setModifyUserId(String modifyUserId) {
        this.modifyUserId = modifyUserId;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Attendance)) return false;
        final Attendance other = (Attendance) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$bookingId = this.getBookingId();
        final Object other$bookingId = other.getBookingId();
        if (this$bookingId == null ? other$bookingId != null : !this$bookingId.equals(other$bookingId)) return false;
        final Object this$eventId = this.getEventId();
        final Object other$eventId = other.getEventId();
        if (this$eventId == null ? other$eventId != null : !this$eventId.equals(other$eventId)) return false;
        final Object this$eventLocationId = this.getEventLocationId();
        final Object other$eventLocationId = other.getEventLocationId();
        if (this$eventLocationId == null ? other$eventLocationId != null : !this$eventLocationId.equals(other$eventLocationId))
            return false;
        final Object this$eventDate = this.getEventDate();
        final Object other$eventDate = other.getEventDate();
        if (this$eventDate == null ? other$eventDate != null : !this$eventDate.equals(other$eventDate)) return false;
        final Object this$period = this.getPeriod();
        final Object other$period = other.getPeriod();
        if (this$period == null ? other$period != null : !this$period.equals(other$period)) return false;
        final Object this$prisonId = this.getPrisonId();
        final Object other$prisonId = other.getPrisonId();
        if (this$prisonId == null ? other$prisonId != null : !this$prisonId.equals(other$prisonId)) return false;
        final Object this$paid = this.getPaid();
        final Object other$paid = other.getPaid();
        if (this$paid == null ? other$paid != null : !this$paid.equals(other$paid)) return false;
        final Object this$attended = this.getAttended();
        final Object other$attended = other.getAttended();
        if (this$attended == null ? other$attended != null : !this$attended.equals(other$attended)) return false;
        final Object this$absentReason = this.getAbsentReason();
        final Object other$absentReason = other.getAbsentReason();
        if (this$absentReason == null ? other$absentReason != null : !this$absentReason.equals(other$absentReason))
            return false;
        final Object this$comments = this.getComments();
        final Object other$comments = other.getComments();
        if (this$comments == null ? other$comments != null : !this$comments.equals(other$comments)) return false;
        final Object this$caseNoteId = this.getCaseNoteId();
        final Object other$caseNoteId = other.getCaseNoteId();
        if (this$caseNoteId == null ? other$caseNoteId != null : !this$caseNoteId.equals(other$caseNoteId))
            return false;
        final Object this$createDateTime = this.getCreateDateTime();
        final Object other$createDateTime = other.getCreateDateTime();
        if (this$createDateTime == null ? other$createDateTime != null : !this$createDateTime.equals(other$createDateTime))
            return false;
        final Object this$createUserId = this.getCreateUserId();
        final Object other$createUserId = other.getCreateUserId();
        if (this$createUserId == null ? other$createUserId != null : !this$createUserId.equals(other$createUserId))
            return false;
        final Object this$modifyDateTime = this.getModifyDateTime();
        final Object other$modifyDateTime = other.getModifyDateTime();
        if (this$modifyDateTime == null ? other$modifyDateTime != null : !this$modifyDateTime.equals(other$modifyDateTime))
            return false;
        final Object this$modifyUserId = this.getModifyUserId();
        final Object other$modifyUserId = other.getModifyUserId();
        if (this$modifyUserId == null ? other$modifyUserId != null : !this$modifyUserId.equals(other$modifyUserId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Attendance;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $bookingId = this.getBookingId();
        result = result * PRIME + ($bookingId == null ? 43 : $bookingId.hashCode());
        final Object $eventId = this.getEventId();
        result = result * PRIME + ($eventId == null ? 43 : $eventId.hashCode());
        final Object $eventLocationId = this.getEventLocationId();
        result = result * PRIME + ($eventLocationId == null ? 43 : $eventLocationId.hashCode());
        final Object $eventDate = this.getEventDate();
        result = result * PRIME + ($eventDate == null ? 43 : $eventDate.hashCode());
        final Object $period = this.getPeriod();
        result = result * PRIME + ($period == null ? 43 : $period.hashCode());
        final Object $prisonId = this.getPrisonId();
        result = result * PRIME + ($prisonId == null ? 43 : $prisonId.hashCode());
        final Object $paid = this.getPaid();
        result = result * PRIME + ($paid == null ? 43 : $paid.hashCode());
        final Object $attended = this.getAttended();
        result = result * PRIME + ($attended == null ? 43 : $attended.hashCode());
        final Object $absentReason = this.getAbsentReason();
        result = result * PRIME + ($absentReason == null ? 43 : $absentReason.hashCode());
        final Object $comments = this.getComments();
        result = result * PRIME + ($comments == null ? 43 : $comments.hashCode());
        final Object $caseNoteId = this.getCaseNoteId();
        result = result * PRIME + ($caseNoteId == null ? 43 : $caseNoteId.hashCode());
        final Object $createDateTime = this.getCreateDateTime();
        result = result * PRIME + ($createDateTime == null ? 43 : $createDateTime.hashCode());
        final Object $createUserId = this.getCreateUserId();
        result = result * PRIME + ($createUserId == null ? 43 : $createUserId.hashCode());
        final Object $modifyDateTime = this.getModifyDateTime();
        result = result * PRIME + ($modifyDateTime == null ? 43 : $modifyDateTime.hashCode());
        final Object $modifyUserId = this.getModifyUserId();
        result = result * PRIME + ($modifyUserId == null ? 43 : $modifyUserId.hashCode());
        return result;
    }

    public String toString() {
        return "Attendance(id=" + this.getId() + ", bookingId=" + this.getBookingId() + ", eventId=" + this.getEventId() + ", eventLocationId=" + this.getEventLocationId() + ", eventDate=" + this.getEventDate() + ", period=" + this.getPeriod() + ", prisonId=" + this.getPrisonId() + ", paid=" + this.getPaid() + ", attended=" + this.getAttended() + ", absentReason=" + this.getAbsentReason() + ", comments=" + this.getComments() + ", caseNoteId=" + this.getCaseNoteId() + ", createDateTime=" + this.getCreateDateTime() + ", createUserId=" + this.getCreateUserId() + ", modifyDateTime=" + this.getModifyDateTime() + ", modifyUserId=" + this.getModifyUserId() + ")";
    }

    public AttendanceBuilder toBuilder() {
        return new AttendanceBuilder().id(this.id).bookingId(this.bookingId).eventId(this.eventId).eventLocationId(this.eventLocationId).eventDate(this.eventDate).period(this.period).prisonId(this.prisonId).paid(this.paid).attended(this.attended).absentReason(this.absentReason).comments(this.comments).caseNoteId(this.caseNoteId).createDateTime(this.createDateTime).createUserId(this.createUserId).modifyDateTime(this.modifyDateTime).modifyUserId(this.modifyUserId);
    }

    public static class AttendanceBuilder {
        private Long id;
        private @NotNull Long bookingId;
        private @NotNull Long eventId;
        private @NotNull Long eventLocationId;
        private @NotNull LocalDate eventDate;
        private @NotNull TimePeriod period;
        private @NotNull @Size(max = 6) String prisonId;
        private Boolean paid;
        private Boolean attended;
        private AbsentReason absentReason;
        private @Size(max = 240) String comments;
        private Long caseNoteId;
        private LocalDateTime createDateTime;
        private String createUserId;
        private LocalDateTime modifyDateTime;
        private String modifyUserId;

        AttendanceBuilder() {
        }

        public Attendance.AttendanceBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public Attendance.AttendanceBuilder bookingId(@NotNull Long bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public Attendance.AttendanceBuilder eventId(@NotNull Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public Attendance.AttendanceBuilder eventLocationId(@NotNull Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public Attendance.AttendanceBuilder eventDate(@NotNull LocalDate eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public Attendance.AttendanceBuilder period(@NotNull TimePeriod period) {
            this.period = period;
            return this;
        }

        public Attendance.AttendanceBuilder prisonId(@NotNull @Size(max = 6) String prisonId) {
            this.prisonId = prisonId;
            return this;
        }

        public Attendance.AttendanceBuilder paid(Boolean paid) {
            this.paid = paid;
            return this;
        }

        public Attendance.AttendanceBuilder attended(Boolean attended) {
            this.attended = attended;
            return this;
        }

        public Attendance.AttendanceBuilder absentReason(AbsentReason absentReason) {
            this.absentReason = absentReason;
            return this;
        }

        public Attendance.AttendanceBuilder comments(@Size(max = 240) String comments) {
            this.comments = comments;
            return this;
        }

        public Attendance.AttendanceBuilder caseNoteId(Long caseNoteId) {
            this.caseNoteId = caseNoteId;
            return this;
        }

        public Attendance.AttendanceBuilder createDateTime(LocalDateTime createDateTime) {
            this.createDateTime = createDateTime;
            return this;
        }

        public Attendance.AttendanceBuilder createUserId(String createUserId) {
            this.createUserId = createUserId;
            return this;
        }

        public Attendance.AttendanceBuilder modifyDateTime(LocalDateTime modifyDateTime) {
            this.modifyDateTime = modifyDateTime;
            return this;
        }

        public Attendance.AttendanceBuilder modifyUserId(String modifyUserId) {
            this.modifyUserId = modifyUserId;
            return this;
        }

        public Attendance build() {
            return new Attendance(id, bookingId, eventId, eventLocationId, eventDate, period, prisonId, paid, attended, absentReason, comments, caseNoteId, createDateTime, createUserId, modifyDateTime, modifyUserId);
        }

        public String toString() {
            return "Attendance.AttendanceBuilder(id=" + this.id + ", bookingId=" + this.bookingId + ", eventId=" + this.eventId + ", eventLocationId=" + this.eventLocationId + ", eventDate=" + this.eventDate + ", period=" + this.period + ", prisonId=" + this.prisonId + ", paid=" + this.paid + ", attended=" + this.attended + ", absentReason=" + this.absentReason + ", comments=" + this.comments + ", caseNoteId=" + this.caseNoteId + ", createDateTime=" + this.createDateTime + ", createUserId=" + this.createUserId + ", modifyDateTime=" + this.modifyDateTime + ", modifyUserId=" + this.modifyUserId + ")";
        }
    }
}
