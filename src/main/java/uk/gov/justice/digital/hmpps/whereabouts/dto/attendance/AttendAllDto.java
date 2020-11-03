package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@ApiModel(description = "Attend all parameters")
public class AttendAllDto {
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

    public AttendAllDto(@NotNull Set<BookingActivity> bookingActivities, @NotNull Long eventLocationId, @NotNull TimePeriod period, @NotNull String prisonId, @NotNull LocalDate eventDate) {
        this.bookingActivities = bookingActivities;
        this.eventLocationId = eventLocationId;
        this.period = period;
        this.prisonId = prisonId;
        this.eventDate = eventDate;
    }

    public AttendAllDto() {
    }

    public static AttendAllDtoBuilder builder() {
        return new AttendAllDtoBuilder();
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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AttendAllDto)) return false;
        final AttendAllDto other = (AttendAllDto) o;
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
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AttendAllDto;
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
        return result;
    }

    public String toString() {
        return "AttendAllDto(bookingActivities=" + this.getBookingActivities() + ", eventLocationId=" + this.getEventLocationId() + ", period=" + this.getPeriod() + ", prisonId=" + this.getPrisonId() + ", eventDate=" + this.getEventDate() + ")";
    }

    public AttendAllDtoBuilder toBuilder() {
        return new AttendAllDtoBuilder().bookingActivities(this.bookingActivities).eventLocationId(this.eventLocationId).period(this.period).prisonId(this.prisonId).eventDate(this.eventDate);
    }

    public static class AttendAllDtoBuilder {
        private @NotNull Set<BookingActivity> bookingActivities;
        private @NotNull Long eventLocationId;
        private @NotNull TimePeriod period;
        private @NotNull String prisonId;
        private @NotNull LocalDate eventDate;

        AttendAllDtoBuilder() {
        }

        public AttendAllDto.AttendAllDtoBuilder bookingActivities(@NotNull Set<BookingActivity> bookingActivities) {
            this.bookingActivities = bookingActivities;
            return this;
        }

        public AttendAllDto.AttendAllDtoBuilder eventLocationId(@NotNull Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public AttendAllDto.AttendAllDtoBuilder period(@NotNull TimePeriod period) {
            this.period = period;
            return this;
        }

        public AttendAllDto.AttendAllDtoBuilder prisonId(@NotNull String prisonId) {
            this.prisonId = prisonId;
            return this;
        }

        public AttendAllDto.AttendAllDtoBuilder eventDate(@NotNull LocalDate eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public AttendAllDto build() {
            return new AttendAllDto(bookingActivities, eventLocationId, period, prisonId, eventDate);
        }

        public String toString() {
            return "AttendAllDto.AttendAllDtoBuilder(bookingActivities=" + this.bookingActivities + ", eventLocationId=" + this.eventLocationId + ", period=" + this.period + ", prisonId=" + this.prisonId + ", eventDate=" + this.eventDate + ")";
        }
    }
}
