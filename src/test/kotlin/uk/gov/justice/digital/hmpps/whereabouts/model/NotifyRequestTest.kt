package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.LocalDate
import java.time.LocalDateTime

class NotifyRequestTest {
  val lastName = "Last Name"
  val firstName = "First Name"
  val dateOfBirth = LocalDate.of(1977, 2, 1)
  val courtName = "The Court"
  val comments = "Comments"
  val courtId = "TC"
  val courtHearingType = CourtHearingType.APPEAL
  val prisonId = "WWI"
  val prisonName = "Prison name"
  val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 12, 34)
  val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
  val endAfternoonDateTime = LocalDateTime.of(2022, 1, 1, 14, 0, 0)

  var videoLinkBooking =
    DataHelpers.makeVideoLinkBooking(
      id = 1L,
      courtName = courtName,
      courtId = courtId,
      courtHearingType = courtHearingType,
      offenderBookingId = 1L,
      prisonId = prisonId,
    ).apply {
      addPreAppointment(1L, 10L, startDateTime, endDateTime)
      addMainAppointment(2L, 20L, startDateTime, endDateTime)
      addPostAppointment(3L, 30L, startDateTime, endAfternoonDateTime)
    }

  @Test
  fun `validate correct formatting when all appointments present`() {
    val request = NotifyRequest(
      lastName = lastName,
      firstName = firstName,
      dateOfBirth = dateOfBirth,
      mainHearing = videoLinkBooking.appointments[HearingType.MAIN]!!,
      postHearing = videoLinkBooking.appointments[HearingType.POST]!!,
      preHearing = videoLinkBooking.appointments[HearingType.PRE]!!,
      comments = comments,
      prisonName = prisonName,
      courtName = courtName,
    )

    val map = request.constructMapOfNotifyRequest()
    Assertions.assertThat(map["firstName"]).isEqualTo(firstName)
    Assertions.assertThat(map["lastName"]).isEqualTo(lastName)
    Assertions.assertThat(map["dateOfBirth"]).isEqualTo("01/02/1977")
    Assertions.assertThat(map["date"]).isEqualTo("01/01/2022")
    Assertions.assertThat(map["mainHearingStartAndEndTime"]).isEqualTo("10:12 to 11:00")
    Assertions.assertThat(map["preHearingStartAndEndTime"]).isEqualTo("10:12 to 11:00")
    Assertions.assertThat(map["postHearingStartAndEndTime"]).isEqualTo("10:12 to 14:00")
    Assertions.assertThat(map["comments"]).isEqualTo(comments)
    Assertions.assertThat(map["prison"]).isEqualTo(prisonName)
    Assertions.assertThat(map["hearingLocation"]).isEqualTo(courtName)
  }

  @Test
  fun `validate correct formatting when post appointments not present`() {
    val request = NotifyRequest(
      lastName = lastName,
      firstName = firstName,
      dateOfBirth = dateOfBirth,
      mainHearing = videoLinkBooking.appointments[HearingType.MAIN]!!,
      postHearing = null,
      preHearing = videoLinkBooking.appointments[HearingType.PRE]!!,
      comments = comments,
      prisonName = prisonName,
      courtName = courtName,
    )

    val map = request.constructMapOfNotifyRequest()
    Assertions.assertThat(map["firstName"]).isEqualTo(firstName)
    Assertions.assertThat(map["lastName"]).isEqualTo(lastName)
    Assertions.assertThat(map["dateOfBirth"]).isEqualTo("01/02/1977")
    Assertions.assertThat(map["date"]).isEqualTo("01/01/2022")
    Assertions.assertThat(map["mainHearingStartAndEndTime"]).isEqualTo("10:12 to 11:00")
    Assertions.assertThat(map["preHearingStartAndEndTime"]).isEqualTo("10:12 to 11:00")
    Assertions.assertThat(map["postHearingStartAndEndTime"]).isEqualTo("None requested")
    Assertions.assertThat(map["comments"]).isEqualTo(comments)
    Assertions.assertThat(map["prison"]).isEqualTo(prisonName)
    Assertions.assertThat(map["hearingLocation"]).isEqualTo(courtName)
  }

  @Test
  fun `validate correct formatting comment not present`() {
    val request = NotifyRequest(
      lastName = lastName,
      firstName = firstName,
      dateOfBirth = dateOfBirth,
      mainHearing = videoLinkBooking.appointments[HearingType.MAIN]!!,
      postHearing = videoLinkBooking.appointments[HearingType.POST]!!,
      preHearing = videoLinkBooking.appointments[HearingType.PRE]!!,
      comments = null,
      prisonName = prisonName,
      courtName = courtName,
    )

    val map = request.constructMapOfNotifyRequest()
    Assertions.assertThat(map["firstName"]).isEqualTo(firstName)
    Assertions.assertThat(map["lastName"]).isEqualTo(lastName)
    Assertions.assertThat(map["dateOfBirth"]).isEqualTo("01/02/1977")
    Assertions.assertThat(map["date"]).isEqualTo("01/01/2022")
    Assertions.assertThat(map["mainHearingStartAndEndTime"]).isEqualTo("10:12 to 11:00")
    Assertions.assertThat(map["preHearingStartAndEndTime"]).isEqualTo("10:12 to 11:00")
    Assertions.assertThat(map["postHearingStartAndEndTime"]).isEqualTo("10:12 to 14:00")
    Assertions.assertThat(map["comments"]).isEqualTo("None entered")
    Assertions.assertThat(map["prison"]).isEqualTo(prisonName)
    Assertions.assertThat(map["hearingLocation"]).isEqualTo(courtName)
  }
}
