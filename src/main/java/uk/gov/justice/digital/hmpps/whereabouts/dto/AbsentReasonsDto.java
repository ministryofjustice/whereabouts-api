package uk.gov.justice.digital.hmpps.whereabouts.dto;

import io.swagger.annotations.ApiModelProperty;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import java.util.Set;

public class AbsentReasonsDto {
    @ApiModelProperty(value = "List of paid absent reasons", example = "[AcceptableAbsence]")
    private Set<AbsentReason> paidReasons;

    @ApiModelProperty(value = "List of unpaid absent reasons", example = "[UnacceptableAbsence]")
    private Set<AbsentReason> unpaidReasons;

    @ApiModelProperty(value = "List of reasons that trigger IEP Warnings", example = "[Refused]")
    private Set<AbsentReason> triggersIEPWarning;

    public AbsentReasonsDto(Set<AbsentReason> paidReasons, Set<AbsentReason> unpaidReasons, Set<AbsentReason> triggersIEPWarning) {
        this.paidReasons = paidReasons;
        this.unpaidReasons = unpaidReasons;
        this.triggersIEPWarning = triggersIEPWarning;
    }

    public AbsentReasonsDto() {
    }

    public Set<AbsentReason> getPaidReasons() {
        return this.paidReasons;
    }

    public Set<AbsentReason> getUnpaidReasons() {
        return this.unpaidReasons;
    }

    public Set<AbsentReason> getTriggersIEPWarning() {
        return this.triggersIEPWarning;
    }

    public void setPaidReasons(Set<AbsentReason> paidReasons) {
        this.paidReasons = paidReasons;
    }

    public void setUnpaidReasons(Set<AbsentReason> unpaidReasons) {
        this.unpaidReasons = unpaidReasons;
    }

    public void setTriggersIEPWarning(Set<AbsentReason> triggersIEPWarning) {
        this.triggersIEPWarning = triggersIEPWarning;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AbsentReasonsDto)) return false;
        final AbsentReasonsDto other = (AbsentReasonsDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$paidReasons = this.getPaidReasons();
        final Object other$paidReasons = other.getPaidReasons();
        if (this$paidReasons == null ? other$paidReasons != null : !this$paidReasons.equals(other$paidReasons))
            return false;
        final Object this$unpaidReasons = this.getUnpaidReasons();
        final Object other$unpaidReasons = other.getUnpaidReasons();
        if (this$unpaidReasons == null ? other$unpaidReasons != null : !this$unpaidReasons.equals(other$unpaidReasons))
            return false;
        final Object this$triggersIEPWarning = this.getTriggersIEPWarning();
        final Object other$triggersIEPWarning = other.getTriggersIEPWarning();
        if (this$triggersIEPWarning == null ? other$triggersIEPWarning != null : !this$triggersIEPWarning.equals(other$triggersIEPWarning))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AbsentReasonsDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $paidReasons = this.getPaidReasons();
        result = result * PRIME + ($paidReasons == null ? 43 : $paidReasons.hashCode());
        final Object $unpaidReasons = this.getUnpaidReasons();
        result = result * PRIME + ($unpaidReasons == null ? 43 : $unpaidReasons.hashCode());
        final Object $triggersIEPWarning = this.getTriggersIEPWarning();
        result = result * PRIME + ($triggersIEPWarning == null ? 43 : $triggersIEPWarning.hashCode());
        return result;
    }

    public String toString() {
        return "AbsentReasonsDto(paidReasons=" + this.getPaidReasons() + ", unpaidReasons=" + this.getUnpaidReasons() + ", triggersIEPWarning=" + this.getTriggersIEPWarning() + ")";
    }
}
