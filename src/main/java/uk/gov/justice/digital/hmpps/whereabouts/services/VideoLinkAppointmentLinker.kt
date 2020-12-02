package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import javax.transaction.Transactional

typealias AppointmentFinder = (PrisonAppointment?) -> VideoLinkAppointment?

@Service
class VideoLinkAppointmentLinker(
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val prisonApiService: PrisonApiService
) {

  @PreAuthorize("hasAnyRole('ROLE_VIDEO_APPOINTMENT_LINKER')")
  fun linkAppointments() {
    getBookingIdsOfUnlinkedAppointments()
      .also { log.info("Found unlinked MAIN appointments for ${it.size} booking Ids") }
      .forEach { linkAppointmentsForOffenderBookingId(it) }
  }

  @Transactional
  fun getBookingIdsOfUnlinkedAppointments(): List<Long> {
    val bookingIds = videoLinkAppointmentRepository.bookingIdsOfUnlinkedAppointments()
    log.info("Found unlinked VideoLinkAppointments for ${bookingIds.size} bookingIds")
    return bookingIds
  }

  @Transactional
  fun linkAppointmentsForOffenderBookingId(bookingId: Long) {
    val bookings = videoLinkBookingsForOffenderBookingId(bookingId)

    log.info("bookingId $bookingId: Creating ${bookings.size} VideoLinkBookings")

    videoLinkBookingRepository.saveAll(bookings)
  }

  fun videoLinkBookingsForOffenderBookingId(bookingId: Long): List<VideoLinkBooking> {
    val mainAppointments = videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(bookingId)

    log.info("bookingId $bookingId: Found ${mainAppointments.size} MAIN VideoLinkAppointments")

    val prisonAppointmentsById = getPrisonAppointments(mainAppointments.map { it.appointmentId })
      .associateBy { it.eventId }

    val findPreAppointmentFor = preAppointmentFinder(bookingId)
    val findPostAppointmentFor = postAppointmentFinder(bookingId)

    return mainAppointments
      .filter { prisonAppointmentsById.containsKey(it.appointmentId) }
      .map { main ->
        val mainPrisonAppointment = prisonAppointmentsById[main.appointmentId]

        VideoLinkBooking(
          main = main,
          pre = findPreAppointmentFor(mainPrisonAppointment),
          post = findPostAppointmentFor(mainPrisonAppointment),
        )
      }
  }

  private fun getPrisonAppointments(appointmentIds: List<Long>) =
    appointmentIds
      .map { prisonApiService.getPrisonAppointment(it) }
      .filterNotNull()

  private fun preAppointmentFinder(bookingId: Long): AppointmentFinder {
    val appointments = videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(bookingId)
    val appointmentsByAppointmentId = appointments.associateBy { it.appointmentId }
    val prisonAppointments = getPrisonAppointments(appointments.map { it.appointmentId })

    val appointmentsByEndTime = prisonAppointments
      .associateBy({ it.endTime }, { appointmentsByAppointmentId[it.eventId] })
      .toMutableMap()

    log.info("bookingId $bookingId: Found ${appointmentsByAppointmentId.size} PRE VideoLinkAppointments, Matched ${appointmentsByEndTime.size} with prison appointments")

    return ({ mainAppointment -> appointmentsByEndTime.remove(mainAppointment?.startTime) })
  }

  private fun postAppointmentFinder(bookingId: Long): AppointmentFinder {
    val appointments = videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(bookingId)
    val appointmentsByAppointmentId = appointments.associateBy { it.appointmentId }
    val prisonAppointments = getPrisonAppointments(appointments.map { it.appointmentId })

    val appointmentsByStartTime = prisonAppointments
      .associateBy({ it.startTime }, { appointmentsByAppointmentId[it.eventId] })
      .toMutableMap()

    log.info("bookingId $bookingId: Found ${appointmentsByAppointmentId.size} POST VideoLinkAppointments, Matched ${appointmentsByStartTime.size} with prison appointments")

    return ({ mainAppointment -> appointmentsByStartTime.remove(mainAppointment?.endTime) })
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    const val CHUNK_SIZE = 200
  }
}
