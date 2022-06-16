package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.PrisonerScheduleDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsenceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendAllDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceChangeDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceHistoryDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceSummary
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendancesDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.OffenderAttendance
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.AttendanceChange
import uk.gov.justice.digital.hmpps.whereabouts.model.AttendanceChangeValues
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceChangesRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Predicate
import java.util.stream.Collectors
import javax.transaction.Transactional

@Service
class AttendanceService(
  private val attendanceRepository: AttendanceRepository,
  private val attendanceChangesRepository: AttendanceChangesRepository,
  private val prisonApiService: PrisonApiService,
  private val iepWarningService: IEPWarningService,
  private val nomisEventOutcomeMapper: NomisEventOutcomeMapper,
  private val telemetryClient: TelemetryClient
) {

  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getAttendanceForEventLocation(
    prisonId: String?,
    eventLocationId: Long?,
    date: LocalDate?,
    period: TimePeriod?
  ): Set<AttendanceDto> {
    val attendance = attendanceRepository
      .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(prisonId, eventLocationId, date, period)

    return attendance.map(this::toAttendanceDto).toSet()
  }

  fun getAbsencesForReason(prisonId: String?, date: LocalDate?, period: TimePeriod?): Set<AttendanceDto> {
    val attendance = attendanceRepository
      .findByPrisonIdAndEventDateAndPeriodAndAbsentReasonNotNull(prisonId, date, period)

    return attendance.map(this::toAttendanceDto).toSet()
  }

  fun getAttendanceForBookings(
    prisonId: String?,
    bookings: Set<Long?>?,
    date: LocalDate?,
    period: TimePeriod?
  ): Set<AttendanceDto> {
    val attendance = attendanceRepository
      .findByPrisonIdAndBookingIdInAndEventDateAndPeriod(prisonId, bookings, date, period)

    return attendance.map(this::toAttendanceDto).toSet()
  }

  fun getAttendanceForBookingsOverDateRange(
    prisonId: String,
    bookings: Set<Long>,
    fromDate: LocalDate,
    toDate: LocalDate?,
    period: TimePeriod?
  ): Set<AttendanceDto> {

    val periods = if (period == null) setOf(TimePeriod.AM, TimePeriod.PM) else setOf(period)
    val endDate = toDate ?: fromDate

    val attendances = attendanceRepository
      .findByPrisonIdAndBookingIdInAndEventDateBetweenAndPeriodIn(prisonId, bookings, fromDate, endDate, periods)

    return attendances.map(this::toAttendanceDto).toSet()
  }

  @Transactional
  @Throws(AttendanceExists::class)
  fun createAttendance(attendanceDto: CreateAttendanceDto): AttendanceDto {
    val provisionalAttendance = toAttendance(attendanceDto)
    val existing = attendanceRepository.findByPrisonIdAndBookingIdAndEventIdAndEventDateAndPeriod(
      provisionalAttendance.prisonId,
      provisionalAttendance.bookingId,
      provisionalAttendance.eventId,
      provisionalAttendance.eventDate,
      provisionalAttendance.period
    )

    if (existing.size > 0) {
      log.info("Attendance already created")
      throw AttendanceExists()
    }

    val attendance = attendanceRepository.save(toAttendance(attendanceDto))

    postNomisAttendance(attendance)
    iepWarningService.postIEPWarningIfRequired(
      bookingId = attendance.bookingId,
      caseNoteId = attendance.caseNoteId,
      reason = attendance.absentReason,
      subReason = attendance.absentSubReason,
      text = attendance.comments,
      eventDate = attendance.eventDate
    ).ifPresent { caseNoteId: Long? -> attendance.caseNoteId = caseNoteId }
    log.info("attendance created {}", attendance.toBuilder().comments(null))
    return toAttendanceDto(attendance)
  }

  @Transactional
  @Throws(AttendanceNotFound::class, AttendanceLocked::class)
  fun updateAttendance(id: Long, newAttendanceDetails: UpdateAttendanceDto) {
    val attendance = attendanceRepository.findById(id).orElseThrow { AttendanceNotFound() }
    if (isAttendanceLocked(attendance)) {
      log.info("Update attempted on locked attendance, attendance id {}", id)
      throw AttendanceLocked()
    }

    iepWarningService.handleIEPWarningScenarios(attendance, newAttendanceDetails)
      .ifPresent { caseNoteId: Long? -> attendance.caseNoteId = caseNoteId }

    val changedFrom = if (attendance.attended) {
      AttendanceChangeValues.Attended
    } else AttendanceChangeValues.valueOf(attendance.absentReason.toString())

    val changedTo = if (newAttendanceDetails.absentReason != null) {
      AttendanceChangeValues.valueOf(newAttendanceDetails.absentReason.toString())
    } else AttendanceChangeValues.Attended

    attendance.comments = newAttendanceDetails.comments
    attendance.attended = newAttendanceDetails.attended
    attendance.paid = newAttendanceDetails.paid
    attendance.absentReason = newAttendanceDetails.absentReason
    attendance.absentSubReason = newAttendanceDetails.absentSubReason

    attendanceRepository.save(attendance)
    postNomisAttendance(attendance)

    attendanceChangesRepository.save(
      AttendanceChange(
        attendance = attendance,
        changedFrom = changedFrom,
        changedTo = changedTo
      )
    )
  }

  @Transactional
  fun attendAll(attendAll: AttendAllDto): Set<AttendanceDto> {
    val eventOutcome = nomisEventOutcomeMapper.getEventOutcome(
      reason = null,
      subReason = null,
      attended = true,
      paid = true,
      comment = ""
    )

    prisonApiService.putAttendanceForMultipleBookings(attendAll.bookingActivities, eventOutcome)

    val attendances = attendAll.bookingActivities.map { (bookingId, activityId) ->
      Attendance.builder()
        .attended(true)
        .paid(true)
        .bookingId(bookingId)
        .eventId(activityId)
        .eventDate(attendAll.eventDate)
        .eventLocationId(attendAll.eventLocationId)
        .period(attendAll.period)
        .prisonId(attendAll.prisonId)
        .build()
    }.toSet()

    attendanceRepository.saveAll(attendances)

    return attendances.stream().map(this::toAttendanceDto).collect(Collectors.toSet())
  }

  private fun postNomisAttendance(attendance: Attendance) {
    val eventOutcome = nomisEventOutcomeMapper.getEventOutcome(
      reason = attendance.absentReason,
      subReason = attendance.absentSubReason,
      attended = attendance.attended,
      paid = attendance.paid,
      comment = attendance.comments
    )

    log.info("Updating attendance on NOMIS {} {}", attendance.toBuilder().comments(null).build(), eventOutcome)
    prisonApiService.putAttendance(attendance.bookingId, attendance.eventId, eventOutcome)
  }

  private fun isAttendanceLocked(attendance: Attendance): Boolean {
    if (attendance.createDateTime == null) return false
    val dateOfChange =
      if (attendance.modifyDateTime == null) attendance.createDateTime.toLocalDate() else attendance.modifyDateTime.toLocalDate()
    val dateDifference = ChronoUnit.DAYS.between(dateOfChange, LocalDate.now())
    return if (attendance.paid) dateDifference >= 1 else dateDifference >= 7
  }

  fun getPrisonersUnaccountedFor(
    prisonId: String,
    date: LocalDate,
    period: TimePeriod,
  ): List<PrisonerScheduleDto> {
    // grab all scheduled activities
    val scheduledActivities = prisonApiService.getScheduledActivities(prisonId, date, period)
    // grab all the attendances that have taken place
    val attendances = attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(prisonId, period, date, date)
      .map { Pair(it.bookingId, it.eventId) }.toSet()
    // filter to leave the scheduled activities
    return scheduledActivities.filter { !attendances.contains(Pair(it.bookingId, it.eventId)) }
  }

  @Transactional
  fun createAttendances(attendancesDto: AttendancesDto): Set<AttendanceDto> {
    val attendances = attendancesDto.bookingActivities
      .map { (bookingId, activityId) ->
        Attendance.builder()
          .bookingId(bookingId)
          .eventId(activityId)
          .attended(attendancesDto.attended)
          .paid(attendancesDto.paid)
          .absentReason(attendancesDto.reason)
          .comments(attendancesDto.comments)
          .eventDate(attendancesDto.eventDate)
          .eventLocationId(attendancesDto.eventLocationId)
          .period(attendancesDto.period)
          .prisonId(attendancesDto.prisonId)
          .build()
      }
      .toSet()

    attendanceRepository.saveAll(attendances)

    val eventOutcome = nomisEventOutcomeMapper.getEventOutcome(
      reason = attendancesDto.reason,
      subReason = null, // for creating multiple attendances can only specify reasons where sub reason not required
      attended = attendancesDto.attended,
      paid = attendancesDto.paid,
      comment = attendancesDto.comments
    )

    prisonApiService.putAttendanceForMultipleBookings(attendancesDto.bookingActivities, eventOutcome)

    return attendances.stream().map(this::toAttendanceDto).collect(Collectors.toSet())
  }

  fun getAbsencesForReason(
    prisonId: String?,
    absentReason: AbsentReason?,
    fromDate: LocalDate,
    toDate: LocalDate?,
    period: TimePeriod?
  ): List<AbsenceDto> {

    val periods = period?.let { setOf(it) } ?: setOf(TimePeriod.PM, TimePeriod.AM)
    val endDate = toDate ?: fromDate

    val attendances = attendanceRepository
      .findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(prisonId, fromDate, endDate, periods, absentReason)
    val attendanceMap = attendances.associateBy({ it.eventId }, { it })

    val offenderDetails = if (attendanceMap.isNotEmpty()) {
      prisonApiService.getScheduleActivityOffenderData(prisonId, attendanceMap.keys)
    } else emptyList()

    return offenderDetails.map { toAbsenceDto2(it, attendanceMap[it.eventId]!!) }
  }

  @Transactional
  fun deleteAttendancesForOffenderDeleteEvent(offenderNo: String, bookingIds: List<Long>) {
    var totalAttendances = 0
    bookingIds.map { bookingId ->
      val attendances = attendanceRepository.findByBookingId(bookingId)
      log.info("Deleting the following attendance records ${attendances.map { it.id }.joinToString(",")}")

      attendanceRepository.deleteAll(attendances)
      totalAttendances += attendances.size
    }
    telemetryClient.trackEvent(
      "OffenderDelete",
      mapOf("offenderNo" to offenderNo, "count" to totalAttendances.toString()),
      null
    )
  }

  fun getAttendanceChanges(fromDateTime: LocalDateTime, toDateTime: LocalDateTime?): Set<AttendanceChangeDto> {
    val changes =
      if (toDateTime == null) {
        attendanceChangesRepository.findAttendanceChangeByCreateDateTime(fromDateTime)
      } else
        attendanceChangesRepository.findAttendanceChangeByCreateDateTimeBetween(fromDateTime, toDateTime)

    return changes
      .map {
        AttendanceChangeDto(
          id = it.id!!,
          attendanceId = it.attendance.id,
          bookingId = it.attendance.bookingId,
          eventId = it.attendance.eventId,
          eventLocationId = it.attendance.eventLocationId,
          changedFrom = it.changedFrom,
          changedTo = it.changedTo,
          changedOn = it.createDateTime,
          changedBy = it.createUserId,
          prisonId = it.attendance.prisonId

        )
      }.toSet()
  }

  private fun countAttendances(offenderAttendances: List<OffenderAttendance>): AttendanceSummary {
    val summary = AttendanceSummary()
    offenderAttendances.forEach { offenderAttendance ->
      when (offenderAttendance.outcome) {
        "UNACAB" -> {
          summary.unacceptableAbsence++
          summary.total++
        }
        "ATT", "UNBEH" -> summary.total++
        "", null -> {}
        else -> {
          summary.acceptableAbsence++
          summary.total++
        }
      }
    }
    return summary
  }

  fun getAttendanceAbsenceSummaryForOffender(
    offenderNo: String,
    fromDate: LocalDate,
    toDate: LocalDate
  ): AttendanceSummary {
    val attendances =
      prisonApiService.getAttendanceForOffender(offenderNo, fromDate, toDate, null, Pageable.unpaged())
    return countAttendances(attendances.content)
  }

  fun getAttendanceDetailsForOffender(
    offenderNo: String,
    fromDate: LocalDate,
    toDate: LocalDate,
    pageable: Pageable
  ): Page<AttendanceHistoryDto> =

    prisonApiService.getAttendanceForOffender(offenderNo, fromDate, toDate, "UNACAB", pageable)
      .map {
        AttendanceHistoryDto(
          eventDate = LocalDate.parse(it.eventDate),
          activity = it.activity,
          activityDescription = it.description,
          location = it.prisonId,
          comments = it.comment,
        )
      }

  private fun offenderDetailsWithPeriod(details: OffenderDetails, period: TimePeriod): OffenderDetails =
    details.copy(
      bookingId = details.bookingId,
      offenderNo = details.offenderNo,
      eventId = details.eventId,
      cellLocation = details.cellLocation,
      eventDate = details.eventDate,
      timeSlot = period.toString(),
      firstName = details.firstName,
      lastName = details.lastName,
      comment = details.comment,
      suspended = details.suspended
    )

  private fun toAbsenceDto(details: OffenderDetails, attendance: Attendance): AbsenceDto =
    AbsenceDto(
      attendanceId = attendance.id,
      bookingId = attendance.bookingId,
      offenderNo = details.offenderNo,
      eventId = attendance.eventId,
      eventLocationId = attendance.eventLocationId,
      eventDate = attendance.eventDate,
      period = TimePeriod.valueOf(details.timeSlot!!),
      reason = attendance.absentReason,
      subReason = attendance.absentSubReason,
      subReasonDescription = attendance.absentSubReason?.label,
      eventDescription = details.comment,
      comments = attendance.comments,
      cellLocation = details.cellLocation,
      firstName = details.firstName,
      lastName = details.lastName,
      suspended = details.suspended
    )

  private fun toAbsenceDto2(details: OffenderDetails, attendance: Attendance): AbsenceDto =
    AbsenceDto(
      attendanceId = attendance.id,
      bookingId = attendance.bookingId,
      offenderNo = details.offenderNo,
      eventId = attendance.eventId,
      eventLocationId = attendance.eventLocationId,
      eventDate = attendance.eventDate,
      period = attendance.period,
      reason = attendance.absentReason,
      subReason = attendance.absentSubReason,
      subReasonDescription = attendance.absentSubReason?.label,
      eventDescription = details.comment,
      comments = attendance.comments,
      cellLocation = details.cellLocation,
      firstName = details.firstName,
      lastName = details.lastName,
      suspended = details.suspended
    )

  private fun findAttendance(bookingId: Long, eventId: Long?, period: TimePeriod): Predicate<in OffenderDetails> {
    return Predicate { (bookingId1, _, eventId1, _, _, timeSlot) ->
      bookingId1 == bookingId && eventId1 == eventId && TimePeriod.valueOf(timeSlot!!) == period
    }
  }

  private fun toAttendance(attendanceDto: CreateAttendanceDto): Attendance =
    Attendance
      .builder()
      .eventLocationId(attendanceDto.eventLocationId)
      .eventDate(attendanceDto.eventDate)
      .eventId(attendanceDto.eventId)
      .bookingId(attendanceDto.bookingId)
      .period(attendanceDto.period)
      .paid(attendanceDto.paid)
      .attended(attendanceDto.attended)
      .prisonId(attendanceDto.prisonId)
      .absentReason(attendanceDto.absentReason)
      .absentSubReason(attendanceDto.absentSubReason)
      .comments(attendanceDto.comments)
      .build()

  private fun toAttendanceDto(attendanceData: Attendance): AttendanceDto =
    AttendanceDto.builder()
      .id(attendanceData.id)
      .eventDate(attendanceData.eventDate)
      .eventId(attendanceData.eventId)
      .bookingId(attendanceData.bookingId)
      .period(attendanceData.period)
      .paid(attendanceData.paid)
      .attended(attendanceData.attended)
      .prisonId(attendanceData.prisonId)
      .absentReason(attendanceData.absentReason)
      .absentSubReason(attendanceData.absentSubReason)
      .eventLocationId(attendanceData.eventLocationId)
      .comments(attendanceData.comments)
      .createUserId(attendanceData.createUserId)
      .createDateTime(attendanceData.createDateTime)
      .caseNoteId(attendanceData.caseNoteId)
      .locked(isAttendanceLocked(attendanceData))
      .modifyDateTime(attendanceData.modifyDateTime)
      .modifyUserId(attendanceData.modifyUserId)
      .build()
}
