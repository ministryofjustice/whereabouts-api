package uk.gov.justice.digital.hmpps.whereabouts.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsentReasonsDto {
    @ApiModelProperty(value = "List of paid absent reasons", example = "[AcceptableAbsence]")
    private Set<AbsentReason> paidReasons;

    @ApiModelProperty(value = "List of unpaid absent reasons", example = "[UnacceptableAbsence]")
    private Set<AbsentReason> unpaidReasons;

    @ApiModelProperty(value = "List of reasons that trigger IEP Warnings", example = "[Refused]")
    private Set<AbsentReason> triggersIEPWarning;
}
