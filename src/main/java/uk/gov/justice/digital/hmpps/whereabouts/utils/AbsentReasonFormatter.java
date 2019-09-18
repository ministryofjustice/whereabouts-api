package uk.gov.justice.digital.hmpps.whereabouts.utils;

import static org.apache.commons.lang3.StringUtils.*;

public class AbsentReasonFormatter {

    public static String titlecase(final String reason) {

        return capitalize(lowerCase(join(splitByCharacterTypeCamelCase(reason), ' ')));
    }
}
