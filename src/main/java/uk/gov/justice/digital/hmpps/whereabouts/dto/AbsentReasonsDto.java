package uk.gov.justice.digital.hmpps.whereabouts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsentReasonsDto {
    private Set<AbsentReason> paidReasons;
    private Set<AbsentReason> unpaidReasons;
}
