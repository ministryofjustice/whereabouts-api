package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@ApiModel(description = "Attendance details to create")
public class CreateAttendanceDto {
    @ApiModelProperty(required = true, value = "Id of active booking", example = "1")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Id of event", example = "2")
    @NotNull
    private Long eventId;

    @ApiModelProperty(required = true, value = "Id of the location the event is taking place", example = "4")
    @NotNull
    private Long eventLocationId;

    @ApiModelProperty(required = true, value = "Time period for the event", example = "AM")
    @NotNull
    private TimePeriod period;

    @ApiModelProperty(required = true, value = "Id of prison the event is taking place", example = "LEI")
    @NotNull
    private String prisonId;

    @ApiModelProperty(required = true, value = "Flag to indicate the offender attended the event", example = "true")
    @NotNull
    private Boolean attended;

    @ApiModelProperty(required = true, value = "Flag to indicate the offender should be paid", example = "true")
    @NotNull
    private Boolean paid;

    @ApiModelProperty(value = "Reason the offender did not attendance the event", example = "Refused")
    private AbsentReason absentReason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01")
    @NotNull
    private LocalDate eventDate;

    @Size(max = 240)
    @ApiModelProperty(value = "Comments about non attendance. This also gets used for the IEP warning text ")
    private String comments;

    public CreateAttendanceDto(@NotNull Long bookingId, @NotNull Long eventId, @NotNull Long eventLocationId, @NotNull TimePeriod period, @NotNull String prisonId, @NotNull Boolean attended, @NotNull Boolean paid, AbsentReason absentReason, @NotNull LocalDate eventDate, @Size(max = 240) String comments) {
        this.bookingId = bookingId;
        this.eventId = eventId;
        this.eventLocationId = eventLocationId;
        this.period = period;
        this.prisonId = prisonId;
        this.attended = attended;
        this.paid = paid;
        this.absentReason = absentReason;
        this.eventDate = eventDate;
        this.comments = comments;
    }

    public CreateAttendanceDto() {
    }

    public static CreateAttendanceDtoBuilder builder() {
        return new CreateAttendanceDtoBuilder();
    }

    public @NotNull Long getBookingId() {
        return this.bookingId;
    }

    public @NotNull Long getEventId() {
        return this.eventId;
    }

    public @NotNull Long getEventLocationId() {
        return this.eventLocationId;
    }

    public @NotNull TimePeriod getPeriod() {
        return this.period;
    }

    public @NotNull String getPrisonId() {
        return this.prisonId;
    }

    public @NotNull Boolean getAttended() {
        return this.attended;
    }

    public @NotNull Boolean getPaid() {
        return this.paid;
    }

    public AbsentReason getAbsentReason() {
        return this.absentReason;
    }

    public @NotNull LocalDate getEventDate() {
        return this.eventDate;
    }

    public @Size(max = 240) String getComments() {
        return this.comments;
    }

    public void setBookingId(@NotNull Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setEventId(@NotNull Long eventId) {
        this.eventId = eventId;
    }

    public void setEventLocationId(@NotNull Long eventLocationId) {
        this.eventLocationId = eventLocationId;
    }

    public void setPeriod(@NotNull TimePeriod period) {
        this.period = period;
    }

    public void setPrisonId(@NotNull String prisonId) {
        this.prisonId = prisonId;
    }

    public void setAttended(@NotNull Boolean attended) {
        this.attended = attended;
    }

    public void setPaid(@NotNull Boolean paid) {
        this.paid = paid;
    }

    public void setAbsentReason(AbsentReason absentReason) {
        this.absentReason = absentReason;
    }

    public void setEventDate(@NotNull LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public void setComments(@Size(max = 240) String comments) {
        this.comments = comments;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CreateAttendanceDto)) return false;
        final CreateAttendanceDto other = (CreateAttendanceDto) o;
        if (!other.canEqual((Object) this)) return false;
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
        final Object this$paid = this.getPaid();
        final Object other$paid = other.getPaid();
        if (this$paid == null ? other$paid != null : !this$paid.equals(other$paid)) return false;
        final Object this$absentReason = this.getAbsentReason();
        final Object other$absentReason = other.getAbsentReason();
        if (this$absentReason == null ? other$absentReason != null : !this$absentReason.equals(other$absentReason))
            return false;
        final Object this$eventDate = this.getEventDate();
        final Object other$eventDate = other.getEventDate();
        if (this$eventDate == null ? other$eventDate != null : !this$eventDate.equals(other$eventDate)) return false;
        final Object this$comments = this.getComments();
        final Object other$comments = other.getComments();
        if (this$comments == null ? other$comments != null : !this$comments.equals(other$comments)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CreateAttendanceDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
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
        final Object $paid = this.getPaid();
        result = result * PRIME + ($paid == null ? 43 : $paid.hashCode());
        final Object $absentReason = this.getAbsentReason();
        result = result * PRIME + ($absentReason == null ? 43 : $absentReason.hashCode());
        final Object $eventDate = this.getEventDate();
        result = result * PRIME + ($eventDate == null ? 43 : $eventDate.hashCode());
        final Object $comments = this.getComments();
        result = result * PRIME + ($comments == null ? 43 : $comments.hashCode());
        return result;
    }

    public String toString() {
        return "CreateAttendanceDto(bookingId=" + this.getBookingId() + ", eventId=" + this.getEventId() + ", eventLocationId=" + this.getEventLocationId() + ", period=" + this.getPeriod() + ", prisonId=" + this.getPrisonId() + ", attended=" + this.getAttended() + ", paid=" + this.getPaid() + ", absentReason=" + this.getAbsentReason() + ", eventDate=" + this.getEventDate() + ", comments=" + this.getComments() + ")";
    }

    public CreateAttendanceDtoBuilder toBuilder() {
        return new CreateAttendanceDtoBuilder().bookingId(this.bookingId).eventId(this.eventId).eventLocationId(this.eventLocationId).period(this.period).prisonId(this.prisonId).attended(this.attended).paid(this.paid).absentReason(this.absentReason).eventDate(this.eventDate).comments(this.comments);
    }

    public static class CreateAttendanceDtoBuilder {
        private @NotNull Long bookingId;
        private @NotNull Long eventId;
        private @NotNull Long eventLocationId;
        private @NotNull TimePeriod period;
        private @NotNull String prisonId;
        private @NotNull Boolean attended;
        private @NotNull Boolean paid;
        private AbsentReason absentReason;
        private @NotNull LocalDate eventDate;
        private @Size(max = 240) String comments;

        CreateAttendanceDtoBuilder() {
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder bookingId(@NotNull Long bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder eventId(@NotNull Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder eventLocationId(@NotNull Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder period(@NotNull TimePeriod period) {
            this.period = period;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder prisonId(@NotNull String prisonId) {
            this.prisonId = prisonId;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder attended(@NotNull Boolean attended) {
            this.attended = attended;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder paid(@NotNull Boolean paid) {
            this.paid = paid;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder absentReason(AbsentReason absentReason) {
            this.absentReason = absentReason;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder eventDate(@NotNull LocalDate eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public CreateAttendanceDto.CreateAttendanceDtoBuilder comments(@Size(max = 240) String comments) {
            this.comments = comments;
            return this;
        }

        public CreateAttendanceDto build() {
            return new CreateAttendanceDto(bookingId, eventId, eventLocationId, period, prisonId, attended, paid, absentReason, eventDate, comments);
        }

        public String toString() {
            return "CreateAttendanceDto.CreateAttendanceDtoBuilder(bookingId=" + this.bookingId + ", eventId=" + this.eventId + ", eventLocationId=" + this.eventLocationId + ", period=" + this.period + ", prisonId=" + this.prisonId + ", attended=" + this.attended + ", paid=" + this.paid + ", absentReason=" + this.absentReason + ", eventDate=" + this.eventDate + ", comments=" + this.comments + ")";
        }
    }
}
