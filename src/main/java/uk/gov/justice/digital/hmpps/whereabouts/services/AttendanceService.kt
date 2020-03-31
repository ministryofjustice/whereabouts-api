package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.*
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.function.Predicate
import java.util.stream.Collectors
import javax.transaction.Transactional

@Service
open class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val elite2ApiService: Elite2ApiService,
    private val iepWarningService: IEPWarningService,
    private val nomisEventOutcomeMapper: NomisEventOutcomeMapper,
    private val telemetryClient: TelemetryClient) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  open fun getAttendanceForEventLocation(prisonId: String?, eventLocationId: Long?, date: LocalDate?, period: TimePeriod?): Set<AttendanceDto> {
    val attendance = attendanceRepository
        .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(prisonId, eventLocationId, date, period)

    return attendance
        .map { toAttendanceDto(it) }
        .toSet()
  }

  open fun getAbsencesForReason(prisonId: String?, date: LocalDate?, period: TimePeriod?): Set<AttendanceDto> {
    val attendance = attendanceRepository
        .findByPrisonIdAndEventDateAndPeriodAndAbsentReasonNotNull(prisonId, date, period)

    return attendance
        .map { toAttendanceDto(it) }
        .toSet()
  }

  open fun getAttendanceForBookings(prisonId: String?, bookings: Set<Long?>?, date: LocalDate?, period: TimePeriod?): Set<AttendanceDto> {
    val attendance = attendanceRepository
        .findByPrisonIdAndBookingIdInAndEventDateAndPeriod(prisonId, bookings, date, period)

    return attendance.map {  toAttendanceDto(it) }.toSet()
  }

  @Transactional
  @Throws(AttendanceExists::class)
  open fun createAttendance(attendanceDto: CreateAttendanceDto): AttendanceDto {
    val provisionalAttendance = toAttendance(attendanceDto)
    val existing = attendanceRepository.findByPrisonIdAndBookingIdAndEventIdAndEventDateAndPeriod(
        provisionalAttendance.prisonId,
        provisionalAttendance.bookingId,
        provisionalAttendance.eventId,
        provisionalAttendance.eventDate,
        provisionalAttendance.period)

    if (existing.size > 0) {
      log.info("Attendance already created")
      throw AttendanceExists()
    }

    val attendance = attendanceRepository.save(toAttendance(attendanceDto))

    postNomisAttendance(attendance)
    iepWarningService.postIEPWarningIfRequired(
        attendance.bookingId,
        attendance.caseNoteId,
        attendance.absentReason,
        attendance.comments,
        attendance.eventDate
    ).ifPresent { caseNoteId: Long? -> attendance.caseNoteId = caseNoteId }
    log.info("attendance created {}", attendance.toBuilder().comments(null))
    return toAttendanceDto(attendance)
  }

  @Transactional
  @Throws(AttendanceNotFound::class, AttendanceLocked::class)
  open fun updateAttendance(id: Long, newAttendanceDetails: UpdateAttendanceDto) {
    val attendance = attendanceRepository.findById(id).orElseThrow { AttendanceNotFound() }
    if (isAttendanceLocked(attendance)) {
      log.info("Update attempted on locked attendance, attendance id {}", id)
      throw AttendanceLocked()
    }

    iepWarningService.handleIEPWarningScenarios(attendance, newAttendanceDetails)
        .ifPresent { caseNoteId: Long? -> attendance.caseNoteId = caseNoteId }

    attendance.comments = newAttendanceDetails.comments
    attendance.attended = newAttendanceDetails.attended
    attendance.paid = newAttendanceDetails.paid
    attendance.absentReason = newAttendanceDetails.absentReason

    attendanceRepository.save(attendance)
    postNomisAttendance(attendance)
  }

  @Transactional
  open fun attendAll(attendAll: AttendAllDto): Set<AttendanceDto> {
    val eventOutcome = nomisEventOutcomeMapper.getEventOutcome(null, true, true, "")

    elite2ApiService.putAttendanceForMultipleBookings(attendAll.bookingActivities, eventOutcome)

    val attendances = attendAll.bookingActivities
        .stream()
        .map { (bookingId, activityId) ->
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
        }
        .collect(Collectors.toSet())

    attendanceRepository.saveAll(attendances)

    return attendances
        .stream()
        .map { attendanceData: Attendance -> toAttendanceDto(attendanceData) }
        .collect(Collectors.toSet())
  }

  private fun postNomisAttendance(attendance: Attendance) {
    val eventOutcome = nomisEventOutcomeMapper.getEventOutcome(
        attendance.absentReason,
        attendance.attended,
        attendance.paid,
        attendance.comments)

    log.info("Updating attendance on NOMIS {} {}", attendance.toBuilder().comments(null).build(), eventOutcome)
    elite2ApiService.putAttendance(attendance.bookingId, attendance.eventId, eventOutcome)
  }

  private fun isAttendanceLocked(attendance: Attendance): Boolean {
    if (attendance.createDateTime == null) return false
    val dateOfChange = if (attendance.modifyDateTime == null) attendance.createDateTime.toLocalDate() else attendance.modifyDateTime.toLocalDate()
    val dateDifference = ChronoUnit.DAYS.between(dateOfChange, LocalDate.now())
    return if (attendance.paid) dateDifference >= 1 else dateDifference >= 7
  }

  open fun getAttendanceForOffendersThatHaveScheduledActivity(prisonId: String?, date: LocalDate?, period: TimePeriod?): Set<AttendanceDto> {
    val bookingIds = elite2ApiService.getBookingIdsForScheduleActivities(prisonId, date, period)
    val attendances = attendanceRepository.findByPrisonIdAndBookingIdInAndEventDateAndPeriod(prisonId, bookingIds, date, period)
    return attendances.stream().map { attendanceData: Attendance -> toAttendanceDto(attendanceData) }.collect(Collectors.toSet())
  }

  @Transactional
  open fun createAttendances(attendancesDto: AttendancesDto): Set<AttendanceDto> {
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
        attendancesDto.reason,
        attendancesDto.attended,
        attendancesDto.paid,
        attendancesDto.comments)

    elite2ApiService.putAttendanceForMultipleBookings(attendancesDto.bookingActivities, eventOutcome)

    return attendances
        .stream()
        .map { attendanceData: Attendance -> toAttendanceDto(attendanceData) }
        .collect(Collectors.toSet())
  }

  fun getAbsencesForReason(prisonId: String?, absentReason: AbsentReason?, fromDate: LocalDate, toDate: LocalDate?,
                           period: TimePeriod?): Set<AbsenceDto> {

    val periods = if (period == null) setOf(TimePeriod.AM, TimePeriod.PM) else setOf(period)
    val endDate = toDate ?: fromDate

    val offenderDetails = periods.flatMap { timePeriod ->
      elite2ApiService.getScheduleActivityOffenderData(prisonId, fromDate, endDate, timePeriod)
          .map { offenderDetailsWithPeriod(it, timePeriod) }.toSet()
    }

    val attendances = attendanceRepository
        .findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(prisonId, fromDate, endDate, periods, absentReason)

    return attendances.stream()
        .filter { attendance: Attendance -> offenderDetails.stream().anyMatch(findAttendance(attendance.bookingId, attendance.eventId, attendance.period)) }
        .map { attendance: Attendance ->
          val details = offenderDetails.stream()
              .filter(findAttendance(attendance.bookingId, attendance.eventId, attendance.period)).findFirst()
              .orElseThrow()
          toAbsenceDto(details, attendance)
        }
        .collect(Collectors.toSet())
  }

  @Transactional
  fun deleteAttendances(offenderNo: String) {
    val bookingId = elite2ApiService.getOffenderBookingId(offenderNo)
    val attendances = attendanceRepository.findByBookingId(bookingId)

    log.info("Deleting the following attendance records ${attendances.map { it.id }.joinToString(",")}")

    attendanceRepository.deleteAll(attendances)

    telemetryClient.trackEvent("OffenderDelete", mapOf("offenderNo" to offenderNo, "count" to  attendances.size.toString()), null)
  }

  private fun offenderDetailsWithPeriod(details: OffenderDetails, period: TimePeriod): OffenderDetails {
    return details.copy(
        details.bookingId,
        details.offenderNo,
        details.eventId,
        details.cellLocation,
        details.eventDate,
        period.toString(),
        details.firstName,
        details.lastName,
        details.comment,
        details.suspended
    )
  }

  private fun toAbsenceDto(details: OffenderDetails, attendance: Attendance): AbsenceDto {
    return AbsenceDto(
        attendance.id,
        attendance.bookingId,
        details.offenderNo,
        attendance.eventId,
        attendance.eventLocationId,
        attendance.eventDate,
        TimePeriod.valueOf(details.timeSlot!!),
        attendance.absentReason,
        details.comment,
        attendance.comments,
        details.cellLocation,
        details.firstName,
        details.lastName,
        details.suspended
    )
  }

  private fun findAttendance(bookingId: Long, eventId: Long?, period: TimePeriod): Predicate<in OffenderDetails> {
    return Predicate { (bookingId1, _, eventId1, _, _, timeSlot) ->
      bookingId1 == bookingId &&
          eventId1 == eventId && TimePeriod.valueOf(timeSlot!!) == period
    }
  }

  private fun toAttendance(attendanceDto: CreateAttendanceDto): Attendance {
    return Attendance
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
        .comments(attendanceDto.comments)
        .build()
  }

  private fun toAttendanceDto(attendanceData: Attendance): AttendanceDto {
    return AttendanceDto.builder()
        .id(attendanceData.id)
        .eventDate(attendanceData.eventDate)
        .eventId(attendanceData.eventId)
        .bookingId(attendanceData.bookingId)
        .period(attendanceData.period)
        .paid(attendanceData.paid)
        .attended(attendanceData.attended)
        .prisonId(attendanceData.prisonId)
        .absentReason(attendanceData.absentReason)
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
}
