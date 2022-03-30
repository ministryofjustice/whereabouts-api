package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceDto {
    private Long id;
    private Long bookingId;
    private Long eventId;
    private Long eventLocationId;
    private TimePeriod period;
    private String prisonId;
    private Boolean attended;
    private AbsentReason absentReason;
    private AbsentSubReason absentSubReason;
    private Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    private String comments;
    private LocalDateTime createDateTime;
    private String createUserId;
    private LocalDateTime modifyDateTime;
    private String modifyUserId;
    private Long caseNoteId;
    private Boolean locked;
    private String cellLocation;

    public AttendanceDto(Long id, Long bookingId, Long eventId, Long eventLocationId, TimePeriod period, String prisonId, Boolean attended, AbsentReason absentReason, AbsentSubReason absentSubReason, Boolean paid, LocalDate eventDate, String comments, LocalDateTime createDateTime, String createUserId, LocalDateTime modifyDateTime, String modifyUserId, Long caseNoteId, Boolean locked, String cellLocation) {
        this.id = id;
        this.bookingId = bookingId;
        this.eventId = eventId;
        this.eventLocationId = eventLocationId;
        this.period = period;
        this.prisonId = prisonId;
        this.attended = attended;
        this.absentReason = absentReason;
        this.absentSubReason = absentSubReason;
        this.paid = paid;
        this.eventDate = eventDate;
        this.comments = comments;
        this.createDateTime = createDateTime;
        this.createUserId = createUserId;
        this.modifyDateTime = modifyDateTime;
        this.modifyUserId = modifyUserId;
        this.caseNoteId = caseNoteId;
        this.locked = locked;
        this.cellLocation = cellLocation;
    }

    public AttendanceDto() {
    }

    public static AttendanceDtoBuilder builder() {
        return new AttendanceDtoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getBookingId() {
        return this.bookingId;
    }

    public Long getEventId() {
        return this.eventId;
    }

    public Long getEventLocationId() {
        return this.eventLocationId;
    }

    public TimePeriod getPeriod() {
        return this.period;
    }

    public String getPrisonId() {
        return this.prisonId;
    }

    public Boolean getAttended() {
        return this.attended;
    }

    public AbsentReason getAbsentReason() {
        return this.absentReason;
    }

    public String getAbsentReasonDescription() {
        return absentReason != null ? absentReason.getLabelWithWarning() : null;
    }

    public AbsentSubReason getAbsentSubReason() {
        return this.absentSubReason;
    }

    public String getAbsentSubReasonDescription() {
        return absentSubReason != null ? absentSubReason.getLabel() : null;
    }

    public Boolean getPaid() {
        return this.paid;
    }

    public LocalDate getEventDate() {
        return this.eventDate;
    }

    public String getComments() {
        return this.comments;
    }

    public LocalDateTime getCreateDateTime() {
        return this.createDateTime;
    }

    public String getCreateUserId() {
        return this.createUserId;
    }

    public LocalDateTime getModifyDateTime() {
        return this.modifyDateTime;
    }

    public String getModifyUserId() {
        return this.modifyUserId;
    }

    public Long getCaseNoteId() {
        return this.caseNoteId;
    }

    public Boolean getLocked() {
        return this.locked;
    }

    public String getCellLocation() {
        return this.cellLocation;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public void setEventLocationId(Long eventLocationId) {
        this.eventLocationId = eventLocationId;
    }

    public void setPeriod(TimePeriod period) {
        this.period = period;
    }

    public void setPrisonId(String prisonId) {
        this.prisonId = prisonId;
    }

    public void setAttended(Boolean attended) {
        this.attended = attended;
    }

    public void setAbsentReason(AbsentReason absentReason) {
        this.absentReason = absentReason;
    }

    public void setAbsentSubReason(AbsentSubReason absentSubReason) {
        this.absentSubReason = absentSubReason;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public void setModifyDateTime(LocalDateTime modifyDateTime) {
        this.modifyDateTime = modifyDateTime;
    }

    public void setModifyUserId(String modifyUserId) {
        this.modifyUserId = modifyUserId;
    }

    public void setCaseNoteId(Long caseNoteId) {
        this.caseNoteId = caseNoteId;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public void setCellLocation(String cellLocation) {
        this.cellLocation = cellLocation;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AttendanceDto)) return false;
        final AttendanceDto other = (AttendanceDto) o;
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
        final Object this$period = this.getPeriod();
        final Object other$period = other.getPeriod();
        if (this$period == null ? other$period != null : !this$period.equals(other$period)) return false;
        final Object this$prisonId = this.getPrisonId();
        final Object other$prisonId = other.getPrisonId();
        if (this$prisonId == null ? other$prisonId != null : !this$prisonId.equals(other$prisonId)) return false;
        final Object this$attended = this.getAttended();
        final Object other$attended = other.getAttended();
        if (this$attended == null ? other$attended != null : !this$attended.equals(other$attended)) return false;
        final Object this$absentReason = this.getAbsentReason();
        final Object other$absentReason = other.getAbsentReason();
        if (this$absentReason == null ? other$absentReason != null : !this$absentReason.equals(other$absentReason))
            return false;
        final Object this$absentSubReason = this.getAbsentSubReason();
        final Object other$absentSubReason = other.getAbsentSubReason();
        if (this$absentSubReason == null ? other$absentSubReason != null : !this$absentSubReason.equals(other$absentSubReason))
            return false;
        final Object this$paid = this.getPaid();
        final Object other$paid = other.getPaid();
        if (this$paid == null ? other$paid != null : !this$paid.equals(other$paid)) return false;
        final Object this$eventDate = this.getEventDate();
        final Object other$eventDate = other.getEventDate();
        if (this$eventDate == null ? other$eventDate != null : !this$eventDate.equals(other$eventDate)) return false;
        final Object this$comments = this.getComments();
        final Object other$comments = other.getComments();
        if (this$comments == null ? other$comments != null : !this$comments.equals(other$comments)) return false;
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
        final Object this$caseNoteId = this.getCaseNoteId();
        final Object other$caseNoteId = other.getCaseNoteId();
        if (this$caseNoteId == null ? other$caseNoteId != null : !this$caseNoteId.equals(other$caseNoteId))
            return false;
        final Object this$locked = this.getLocked();
        final Object other$locked = other.getLocked();
        if (this$locked == null ? other$locked != null : !this$locked.equals(other$locked)) return false;
        final Object this$cellLocation = this.getCellLocation();
        final Object other$cellLocation = other.getCellLocation();
        if (this$cellLocation == null ? other$cellLocation != null : !this$cellLocation.equals(other$cellLocation))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AttendanceDto;
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
        final Object $period = this.getPeriod();
        result = result * PRIME + ($period == null ? 43 : $period.hashCode());
        final Object $prisonId = this.getPrisonId();
        result = result * PRIME + ($prisonId == null ? 43 : $prisonId.hashCode());
        final Object $attended = this.getAttended();
        result = result * PRIME + ($attended == null ? 43 : $attended.hashCode());
        final Object $absentReason = this.getAbsentReason();
        result = result * PRIME + ($absentReason == null ? 43 : $absentReason.hashCode());
        final Object $absentSubReason = this.getAbsentSubReason();
        result = result * PRIME + ($absentSubReason == null ? 43 : $absentSubReason.hashCode());
        final Object $paid = this.getPaid();
        result = result * PRIME + ($paid == null ? 43 : $paid.hashCode());
        final Object $eventDate = this.getEventDate();
        result = result * PRIME + ($eventDate == null ? 43 : $eventDate.hashCode());
        final Object $comments = this.getComments();
        result = result * PRIME + ($comments == null ? 43 : $comments.hashCode());
        final Object $createDateTime = this.getCreateDateTime();
        result = result * PRIME + ($createDateTime == null ? 43 : $createDateTime.hashCode());
        final Object $createUserId = this.getCreateUserId();
        result = result * PRIME + ($createUserId == null ? 43 : $createUserId.hashCode());
        final Object $modifyDateTime = this.getModifyDateTime();
        result = result * PRIME + ($modifyDateTime == null ? 43 : $modifyDateTime.hashCode());
        final Object $modifyUserId = this.getModifyUserId();
        result = result * PRIME + ($modifyUserId == null ? 43 : $modifyUserId.hashCode());
        final Object $caseNoteId = this.getCaseNoteId();
        result = result * PRIME + ($caseNoteId == null ? 43 : $caseNoteId.hashCode());
        final Object $locked = this.getLocked();
        result = result * PRIME + ($locked == null ? 43 : $locked.hashCode());
        final Object $cellLocation = this.getCellLocation();
        result = result * PRIME + ($cellLocation == null ? 43 : $cellLocation.hashCode());
        return result;
    }

    public String toString() {
        return "AttendanceDto(id=" + this.getId() + ", bookingId=" + this.getBookingId() + ", eventId=" + this.getEventId() + ", eventLocationId=" + this.getEventLocationId() + ", period=" + this.getPeriod() + ", prisonId=" + this.getPrisonId() + ", attended=" + this.getAttended() + ", absentReason=" + this.getAbsentReason() +", absentSubReason=" + this.getAbsentSubReason() + ", paid=" + this.getPaid() + ", eventDate=" + this.getEventDate() + ", comments=" + this.getComments() + ", createDateTime=" + this.getCreateDateTime() + ", createUserId=" + this.getCreateUserId() + ", modifyDateTime=" + this.getModifyDateTime() + ", modifyUserId=" + this.getModifyUserId() + ", caseNoteId=" + this.getCaseNoteId() + ", locked=" + this.getLocked() + ", cellLocation=" + this.getCellLocation() + ")";
    }

    public AttendanceDtoBuilder toBuilder() {
        return new AttendanceDtoBuilder().id(this.id).bookingId(this.bookingId).eventId(this.eventId).eventLocationId(this.eventLocationId).period(this.period).prisonId(this.prisonId).attended(this.attended).absentReason(this.absentReason).absentSubReason(this.absentSubReason).paid(this.paid).eventDate(this.eventDate).comments(this.comments).createDateTime(this.createDateTime).createUserId(this.createUserId).modifyDateTime(this.modifyDateTime).modifyUserId(this.modifyUserId).caseNoteId(this.caseNoteId).locked(this.locked).cellLocation(this.cellLocation);
    }

    public static class AttendanceDtoBuilder {
        private Long id;
        private Long bookingId;
        private Long eventId;
        private Long eventLocationId;
        private TimePeriod period;
        private String prisonId;
        private Boolean attended;
        private AbsentReason absentReason;
        private AbsentSubReason absentSubReason;
        private Boolean paid;
        private LocalDate eventDate;
        private String comments;
        private LocalDateTime createDateTime;
        private String createUserId;
        private LocalDateTime modifyDateTime;
        private String modifyUserId;
        private Long caseNoteId;
        private Boolean locked;
        private String cellLocation;

        AttendanceDtoBuilder() {
        }

        public AttendanceDto.AttendanceDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder bookingId(Long bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder eventLocationId(Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder period(TimePeriod period) {
            this.period = period;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder prisonId(String prisonId) {
            this.prisonId = prisonId;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder attended(Boolean attended) {
            this.attended = attended;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder absentReason(AbsentReason absentReason) {
            this.absentReason = absentReason;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder absentSubReason(AbsentSubReason absentSubReason) {
            this.absentSubReason = absentSubReason;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder paid(Boolean paid) {
            this.paid = paid;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder eventDate(LocalDate eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder createDateTime(LocalDateTime createDateTime) {
            this.createDateTime = createDateTime;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder createUserId(String createUserId) {
            this.createUserId = createUserId;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder modifyDateTime(LocalDateTime modifyDateTime) {
            this.modifyDateTime = modifyDateTime;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder modifyUserId(String modifyUserId) {
            this.modifyUserId = modifyUserId;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder caseNoteId(Long caseNoteId) {
            this.caseNoteId = caseNoteId;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder locked(Boolean locked) {
            this.locked = locked;
            return this;
        }

        public AttendanceDto.AttendanceDtoBuilder cellLocation(String cellLocation) {
            this.cellLocation = cellLocation;
            return this;
        }

        public AttendanceDto build() {
            return new AttendanceDto(id, bookingId, eventId, eventLocationId, period, prisonId, attended, absentReason, absentSubReason, paid, eventDate, comments, createDateTime, createUserId, modifyDateTime, modifyUserId, caseNoteId, locked, cellLocation);
        }

        public String toString() {
            return "AttendanceDto.AttendanceDtoBuilder(id=" + this.id + ", bookingId=" + this.bookingId + ", eventId=" + this.eventId + ", eventLocationId=" + this.eventLocationId + ", period=" + this.period + ", prisonId=" + this.prisonId + ", attended=" + this.attended + ", absentReason=" + this.absentReason + ", absentSubReason=" + this.absentSubReason + ", paid=" + this.paid + ", eventDate=" + this.eventDate + ", comments=" + this.comments + ", createDateTime=" + this.createDateTime + ", createUserId=" + this.createUserId + ", modifyDateTime=" + this.modifyDateTime + ", modifyUserId=" + this.modifyUserId + ", caseNoteId=" + this.caseNoteId + ", locked=" + this.locked + ", cellLocation=" + this.cellLocation + ")";
        }
    }
}
