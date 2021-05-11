package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

@Service
class AppointmentService(
  private val prisonApiService: PrisonApiService,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getAppointments(agencyId: String, date: LocalDate, timeSlot: TimePeriod?, offenderLocationPrefix: String?, locationId: Long?): List<AppointmentDto> {
    val appointmentsFromPrisonApi = prisonApiService.getScheduledAppointmentsByAgencyAndDate(agencyId, date, timeSlot, locationId)
    val locationFilter = generateOffenderLocationFilter(offenderLocationPrefix, appointmentsFromPrisonApi)
    return appointmentsFromPrisonApi.filter { a -> locationFilter.filterLocations(a)}.map { a -> makeAppointmentDto(a) }.toList()
  }

  private fun generateOffenderLocationFilter(offenderLocationPrefix: String?, appointmentsFromPrisonApi: List<ScheduledAppointmentDto>): LocationFilter {
    if (offenderLocationPrefix == null) {
      return NoOpFilter()
    }
    val offenderNos = appointmentsFromPrisonApi.map { a -> a.offenderNo }.toSet()
    val offenderBookingDetails = prisonApiService.getOffenderDetailsFromOffenderNos(offenderNos)
    val offenderLocationDescriptionByOffenderNo = offenderBookingDetails.associate { b -> b.offenderNo to b.assignedLivingUnitDesc }
    return OffenderLocationFilter(offenderLocationPrefix, offenderLocationDescriptionByOffenderNo)
  }

  private fun makeAppointmentDto(scheduledAppointmentDto: ScheduledAppointmentDto): AppointmentDto {
    return AppointmentDto(
      id = scheduledAppointmentDto.id,
      agencyId = scheduledAppointmentDto.agencyId,
      locationId = scheduledAppointmentDto.locationId,
      appointmentTypeCode = scheduledAppointmentDto.appointmentTypeCode,
      offenderNo = scheduledAppointmentDto.offenderNo,
      startTime = scheduledAppointmentDto.startTime,
      endTime = scheduledAppointmentDto.endTime
    )
  }
}

interface LocationFilter {
  fun filterLocations(appointment: ScheduledAppointmentDto): Boolean
}

class NoOpFilter : LocationFilter {
  override fun filterLocations(appointment: ScheduledAppointmentDto): Boolean {
    return true
  }
}

class OffenderLocationFilter(
  private val offenderLocationPrefix: String,
  private val offenderLocationDescriptionByOffenderNo: Map<String, String>
) : LocationFilter {
  override fun filterLocations(appointment: ScheduledAppointmentDto): Boolean {
    return offenderLocationDescriptionByOffenderNo.getOrDefault(appointment.offenderNo, "").startsWith(offenderLocationPrefix)
  }
}
