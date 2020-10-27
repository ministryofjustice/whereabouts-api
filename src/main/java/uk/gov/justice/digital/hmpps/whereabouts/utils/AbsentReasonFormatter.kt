package uk.gov.justice.digital.hmpps.whereabouts.utils

import org.apache.commons.lang3.StringUtils.capitalize
import org.apache.commons.lang3.StringUtils.join
import org.apache.commons.lang3.StringUtils.lowerCase
import org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase

object AbsentReasonFormatter {
  fun titlecase(reason: String?) = capitalize(lowerCase(join(splitByCharacterTypeCamelCase(reason), ' ')))
}
