package uk.gov.justice.digital.hmpps.whereabouts.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Attendance update details")
public class UpdateAttendanceDto {
    @ApiModelProperty(required = true, value = "Flag to indicate the offender attended the event", example = "true")
    @NotNull
    private Boolean attended;

    @ApiModelProperty(required = true, value = "Flag to indicate the offender should be paid", example = "true")
    @NotNull
    private Boolean paid;

    @ApiModelProperty(value = "Reason the offender did not attendance the event", example = "Refused")
    private AbsentReason absentReason;

    @Length(max = 240)
    @ApiModelProperty(value = "Comments about non attendance. This also gets used for the IEP warning text ")
    private String comments;

    public UpdateAttendanceDto(@NotNull Boolean attended, @NotNull Boolean paid, AbsentReason absentReason, @Length(max = 240) String comments) {
        this.attended = attended;
        this.paid = paid;
        this.absentReason = absentReason;
        this.comments = comments;
    }

    public UpdateAttendanceDto() {
    }

    public static UpdateAttendanceDtoBuilder builder() {
        return new UpdateAttendanceDtoBuilder();
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

    public @Length(max = 240) String getComments() {
        return this.comments;
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

    public void setComments(@Length(max = 240) String comments) {
        this.comments = comments;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UpdateAttendanceDto)) return false;
        final UpdateAttendanceDto other = (UpdateAttendanceDto) o;
        if (!other.canEqual((Object) this)) return false;
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
        final Object this$comments = this.getComments();
        final Object other$comments = other.getComments();
        if (this$comments == null ? other$comments != null : !this$comments.equals(other$comments)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UpdateAttendanceDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $attended = this.getAttended();
        result = result * PRIME + ($attended == null ? 43 : $attended.hashCode());
        final Object $paid = this.getPaid();
        result = result * PRIME + ($paid == null ? 43 : $paid.hashCode());
        final Object $absentReason = this.getAbsentReason();
        result = result * PRIME + ($absentReason == null ? 43 : $absentReason.hashCode());
        final Object $comments = this.getComments();
        result = result * PRIME + ($comments == null ? 43 : $comments.hashCode());
        return result;
    }

    public String toString() {
        return "UpdateAttendanceDto(attended=" + this.getAttended() + ", paid=" + this.getPaid() + ", absentReason=" + this.getAbsentReason() + ", comments=" + this.getComments() + ")";
    }

    public UpdateAttendanceDtoBuilder toBuilder() {
        return new UpdateAttendanceDtoBuilder().attended(this.attended).paid(this.paid).absentReason(this.absentReason).comments(this.comments);
    }

    public static class UpdateAttendanceDtoBuilder {
        private @NotNull Boolean attended;
        private @NotNull Boolean paid;
        private AbsentReason absentReason;
        private @Length(max = 240) String comments;

        UpdateAttendanceDtoBuilder() {
        }

        public UpdateAttendanceDto.UpdateAttendanceDtoBuilder attended(@NotNull Boolean attended) {
            this.attended = attended;
            return this;
        }

        public UpdateAttendanceDto.UpdateAttendanceDtoBuilder paid(@NotNull Boolean paid) {
            this.paid = paid;
            return this;
        }

        public UpdateAttendanceDto.UpdateAttendanceDtoBuilder absentReason(AbsentReason absentReason) {
            this.absentReason = absentReason;
            return this;
        }

        public UpdateAttendanceDto.UpdateAttendanceDtoBuilder comments(@Length(max = 240) String comments) {
            this.comments = comments;
            return this;
        }

        public UpdateAttendanceDto build() {
            return new UpdateAttendanceDto(attended, paid, absentReason, comments);
        }

        public String toString() {
            return "UpdateAttendanceDto.UpdateAttendanceDtoBuilder(attended=" + this.attended + ", paid=" + this.paid + ", absentReason=" + this.absentReason + ", comments=" + this.comments + ")";
        }
    }
}
