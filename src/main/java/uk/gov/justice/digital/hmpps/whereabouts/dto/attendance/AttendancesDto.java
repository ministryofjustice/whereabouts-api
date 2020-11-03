package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@ApiModel(description = "Attend all parameters")
public class AttendancesDto {
    @ApiModelProperty(required = true, value = "Set of active booking and activity ids")
    @NotNull
    private Set<BookingActivity> bookingActivities;

    @ApiModelProperty(required = true, value = "Id of the location the event is taking place", example = "4")
    @NotNull
    private Long eventLocationId;

    @ApiModelProperty(required = true, value = "Time period for the event", example = "AM")
    @NotNull
    private TimePeriod period;

    @ApiModelProperty(required = true, value = "Id of prison the event is taking place", example = "LEI")
    @NotNull
    private String prisonId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01")
    @NotNull
    private LocalDate eventDate;

    @ApiModelProperty(value = "Absent reason", example = "Refused")
    private AbsentReason reason;

    @ApiModelProperty(value = "Indication of attendance", example = "true")
    @NotNull
    private Boolean attended;

    @ApiModelProperty(value = "Indicates that the offender should be paid", example = "true")
    @NotNull
    private Boolean paid;

    @ApiModelProperty(value = "Comment describing the offenders absence", example = "They had a medical appointment scheduled")
    private String comments;

    public AttendancesDto(@NotNull Set<BookingActivity> bookingActivities, @NotNull Long eventLocationId, @NotNull TimePeriod period, @NotNull String prisonId, @NotNull LocalDate eventDate, AbsentReason reason, @NotNull Boolean attended, @NotNull Boolean paid, String comments) {
        this.bookingActivities = bookingActivities;
        this.eventLocationId = eventLocationId;
        this.period = period;
        this.prisonId = prisonId;
        this.eventDate = eventDate;
        this.reason = reason;
        this.attended = attended;
        this.paid = paid;
        this.comments = comments;
    }

    public AttendancesDto() {
    }

    public static AttendancesDtoBuilder builder() {
        return new AttendancesDtoBuilder();
    }

    public @NotNull Set<BookingActivity> getBookingActivities() {
        return this.bookingActivities;
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

    public @NotNull LocalDate getEventDate() {
        return this.eventDate;
    }

    public AbsentReason getReason() {
        return this.reason;
    }

    public @NotNull Boolean getAttended() {
        return this.attended;
    }

    public @NotNull Boolean getPaid() {
        return this.paid;
    }

    public String getComments() {
        return this.comments;
    }

    public void setBookingActivities(@NotNull Set<BookingActivity> bookingActivities) {
        this.bookingActivities = bookingActivities;
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

    public void setEventDate(@NotNull LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public void setReason(AbsentReason reason) {
        this.reason = reason;
    }

    public void setAttended(@NotNull Boolean attended) {
        this.attended = attended;
    }

    public void setPaid(@NotNull Boolean paid) {
        this.paid = paid;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AttendancesDto)) return false;
        final AttendancesDto other = (AttendancesDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$bookingActivities = this.getBookingActivities();
        final Object other$bookingActivities = other.getBookingActivities();
        if (this$bookingActivities == null ? other$bookingActivities != null : !this$bookingActivities.equals(other$bookingActivities))
            return false;
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
        final Object this$eventDate = this.getEventDate();
        final Object other$eventDate = other.getEventDate();
        if (this$eventDate == null ? other$eventDate != null : !this$eventDate.equals(other$eventDate)) return false;
        final Object this$reason = this.getReason();
        final Object other$reason = other.getReason();
        if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
        final Object this$attended = this.getAttended();
        final Object other$attended = other.getAttended();
        if (this$attended == null ? other$attended != null : !this$attended.equals(other$attended)) return false;
        final Object this$paid = this.getPaid();
        final Object other$paid = other.getPaid();
        if (this$paid == null ? other$paid != null : !this$paid.equals(other$paid)) return false;
        final Object this$comments = this.getComments();
        final Object other$comments = other.getComments();
        if (this$comments == null ? other$comments != null : !this$comments.equals(other$comments)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AttendancesDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $bookingActivities = this.getBookingActivities();
        result = result * PRIME + ($bookingActivities == null ? 43 : $bookingActivities.hashCode());
        final Object $eventLocationId = this.getEventLocationId();
        result = result * PRIME + ($eventLocationId == null ? 43 : $eventLocationId.hashCode());
        final Object $period = this.getPeriod();
        result = result * PRIME + ($period == null ? 43 : $period.hashCode());
        final Object $prisonId = this.getPrisonId();
        result = result * PRIME + ($prisonId == null ? 43 : $prisonId.hashCode());
        final Object $eventDate = this.getEventDate();
        result = result * PRIME + ($eventDate == null ? 43 : $eventDate.hashCode());
        final Object $reason = this.getReason();
        result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
        final Object $attended = this.getAttended();
        result = result * PRIME + ($attended == null ? 43 : $attended.hashCode());
        final Object $paid = this.getPaid();
        result = result * PRIME + ($paid == null ? 43 : $paid.hashCode());
        final Object $comments = this.getComments();
        result = result * PRIME + ($comments == null ? 43 : $comments.hashCode());
        return result;
    }

    public String toString() {
        return "AttendancesDto(bookingActivities=" + this.getBookingActivities() + ", eventLocationId=" + this.getEventLocationId() + ", period=" + this.getPeriod() + ", prisonId=" + this.getPrisonId() + ", eventDate=" + this.getEventDate() + ", reason=" + this.getReason() + ", attended=" + this.getAttended() + ", paid=" + this.getPaid() + ", comments=" + this.getComments() + ")";
    }

    public AttendancesDtoBuilder toBuilder() {
        return new AttendancesDtoBuilder().bookingActivities(this.bookingActivities).eventLocationId(this.eventLocationId).period(this.period).prisonId(this.prisonId).eventDate(this.eventDate).reason(this.reason).attended(this.attended).paid(this.paid).comments(this.comments);
    }

    public static class AttendancesDtoBuilder {
        private @NotNull Set<BookingActivity> bookingActivities;
        private @NotNull Long eventLocationId;
        private @NotNull TimePeriod period;
        private @NotNull String prisonId;
        private @NotNull LocalDate eventDate;
        private AbsentReason reason;
        private @NotNull Boolean attended;
        private @NotNull Boolean paid;
        private String comments;

        AttendancesDtoBuilder() {
        }

        public AttendancesDto.AttendancesDtoBuilder bookingActivities(@NotNull Set<BookingActivity> bookingActivities) {
            this.bookingActivities = bookingActivities;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder eventLocationId(@NotNull Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder period(@NotNull TimePeriod period) {
            this.period = period;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder prisonId(@NotNull String prisonId) {
            this.prisonId = prisonId;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder eventDate(@NotNull LocalDate eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder reason(AbsentReason reason) {
            this.reason = reason;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder attended(@NotNull Boolean attended) {
            this.attended = attended;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder paid(@NotNull Boolean paid) {
            this.paid = paid;
            return this;
        }

        public AttendancesDto.AttendancesDtoBuilder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public AttendancesDto build() {
            return new AttendancesDto(bookingActivities, eventLocationId, period, prisonId, eventDate, reason, attended, paid, comments);
        }

        public String toString() {
            return "AttendancesDto.AttendancesDtoBuilder(bookingActivities=" + this.bookingActivities + ", eventLocationId=" + this.eventLocationId + ", period=" + this.period + ", prisonId=" + this.prisonId + ", eventDate=" + this.eventDate + ", reason=" + this.reason + ", attended=" + this.attended + ", paid=" + this.paid + ", comments=" + this.comments + ")";
        }
    }
}
