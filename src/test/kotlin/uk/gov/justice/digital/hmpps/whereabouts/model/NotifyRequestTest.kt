package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.LocalDate
import java.time.LocalDateTime

class NotifyRequestTest {
  val LAST_NAME = "Last Name"
  val FIRST_NAME = "First Name"
  val DATE_OF_BIRTH = LocalDate.of(1977, 2, 1)
  val COURT_NAME = "The Court"
  val COMMENTS = "Comments"
  val COURT_ID = "TC"
  val COURT_HEARING_TYPE = CourtHearingType.APPEAL
  val PRISON_ID = "WWI"
  val PRISON_NAME = "Prison name"
  val START_DATE_TIME = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
  val END_DATE_TIME = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
  val END_AFTERNOON_DATE_TIME = LocalDateTime.of(2022, 1, 1, 14, 0, 0)

  var videoLinkBooking =
    DataHelpers.makeVideoLinkBooking(
      id = 1L,
      courtName = COURT_NAME,
      courtId = COURT_ID,
      courtHearingType = COURT_HEARING_TYPE,
      offenderBookingId = 1L,
      prisonId = PRISON_ID,
    ).apply {
      addPreAppointment(1L, 10L, START_DATE_TIME, END_DATE_TIME)
      addMainAppointment(2L, 20L, START_DATE_TIME, END_DATE_TIME)
      addPostAppointment(3L, 30L, START_DATE_TIME, END_AFTERNOON_DATE_TIME)
    }

  @Test
  fun `validate correct formatting when all appointments present`() {
    val request = NotifyRequest(
      lastName = LAST_NAME,
      firstName = FIRST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      mainHearing = videoLinkBooking.appointments[HearingType.MAIN]!!,
      postHearing = videoLinkBooking.appointments[HearingType.POST]!!,
      preHearing = videoLinkBooking.appointments[HearingType.PRE]!!,
      comments = COMMENTS,
      prisonName = PRISON_NAME,
      courtName = COURT_NAME,
    )

    val map = request.constructMapOfNotifyRequest()
    Assertions.assertThat(map["firstName"]).isEqualTo(FIRST_NAME)
    Assertions.assertThat(map["lastName"]).isEqualTo(LAST_NAME)
    Assertions.assertThat(map["dateOfBirth"]).isEqualTo("01/02/1977")
    Assertions.assertThat(map["date"]).isEqualTo("01/01/2022")
    Assertions.assertThat(map["mainHearingStartAndEndTime"]).isEqualTo("10:00:00 to 11:00:00")
    Assertions.assertThat(map["preHearingStartAndEndTime"]).isEqualTo("10:00:00 to 11:00:00")
    Assertions.assertThat(map["postHearingStartAndEndTime"]).isEqualTo("10:00:00 to 14:00:00")
    Assertions.assertThat(map["comments"]).isEqualTo(COMMENTS)
    Assertions.assertThat(map["prison"]).isEqualTo(PRISON_NAME)
    Assertions.assertThat(map["hearingLocation"]).isEqualTo(COURT_NAME)
  }

  @Test
  fun `validate correct formatting when post appointments not present`() {
    val request = NotifyRequest(
      lastName = LAST_NAME,
      firstName = FIRST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      mainHearing = videoLinkBooking.appointments[HearingType.MAIN]!!,
      postHearing = null,
      preHearing = videoLinkBooking.appointments[HearingType.PRE]!!,
      comments = COMMENTS,
      prisonName = PRISON_NAME,
      courtName = COURT_NAME,
    )

    val map = request.constructMapOfNotifyRequest()
    Assertions.assertThat(map["firstName"]).isEqualTo(FIRST_NAME)
    Assertions.assertThat(map["lastName"]).isEqualTo(LAST_NAME)
    Assertions.assertThat(map["dateOfBirth"]).isEqualTo("01/02/1977")
    Assertions.assertThat(map["date"]).isEqualTo("01/01/2022")
    Assertions.assertThat(map["mainHearingStartAndEndTime"]).isEqualTo("10:00:00 to 11:00:00")
    Assertions.assertThat(map["preHearingStartAndEndTime"]).isEqualTo("10:00:00 to 11:00:00")
    Assertions.assertThat(map["postHearingStartAndEndTime"]).isEqualTo("None requested")
    Assertions.assertThat(map["comments"]).isEqualTo(COMMENTS)
    Assertions.assertThat(map["prison"]).isEqualTo(PRISON_NAME)
    Assertions.assertThat(map["hearingLocation"]).isEqualTo(COURT_NAME)
  }

  @Test
  fun `validate correct formatting comment not present`() {
    val request = NotifyRequest(
      lastName = LAST_NAME,
      firstName = FIRST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      mainHearing = videoLinkBooking.appointments[HearingType.MAIN]!!,
      postHearing = videoLinkBooking.appointments[HearingType.POST]!!,
      preHearing = videoLinkBooking.appointments[HearingType.PRE]!!,
      comments = null,
      prisonName = PRISON_NAME,
      courtName = COURT_NAME,
    )

    val map = request.constructMapOfNotifyRequest()
    Assertions.assertThat(map["firstName"]).isEqualTo(FIRST_NAME)
    Assertions.assertThat(map["lastName"]).isEqualTo(LAST_NAME)
    Assertions.assertThat(map["dateOfBirth"]).isEqualTo("01/02/1977")
    Assertions.assertThat(map["date"]).isEqualTo("01/01/2022")
    Assertions.assertThat(map["mainHearingStartAndEndTime"]).isEqualTo("10:00:00 to 11:00:00")
    Assertions.assertThat(map["preHearingStartAndEndTime"]).isEqualTo("10:00:00 to 11:00:00")
    Assertions.assertThat(map["postHearingStartAndEndTime"]).isEqualTo("10:00:00 to 14:00:00")
    Assertions.assertThat(map["comments"]).isEqualTo("None entered")
    Assertions.assertThat(map["prison"]).isEqualTo(PRISON_NAME)
    Assertions.assertThat(map["hearingLocation"]).isEqualTo(COURT_NAME)
  }
}
