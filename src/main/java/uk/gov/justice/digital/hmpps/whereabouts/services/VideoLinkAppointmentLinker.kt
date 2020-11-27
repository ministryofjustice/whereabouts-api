package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

  fun linkAppointments() {

    getBookingIdsOfUnlinkedAppointments().forEach { linkAppointmentsForOffenderBookingId(it) }
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
    val prisonAppointments = getPrisonVideoLinkAppointmentsForBookingId(bookingId)
    val prisonAppointmentsById = prisonAppointments.associateBy { it.eventId }

    val mainAppointments = videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(bookingId)

    log.info("bookingId $bookingId: Found ${mainAppointments.size} MAIN VideoLinkAppointments")

    val findPreAppointmentFor = preAppointmentFinder(bookingId, prisonAppointments)
    val findPostAppointmentFor = postAppointmentFinder(bookingId, prisonAppointments)

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

  fun getPrisonVideoLinkAppointmentsForBookingId(bookingId: Long): List<PrisonAppointment> {
    val prisonAppointments = mutableListOf<PrisonAppointment>()
    var offset = 0

    do {
      val chunk = prisonApiService
        .getPrisonAppointmentsForBookingId(bookingId, offset, CHUNK_SIZE)
        .filter { it.eventSubType == "VLB" }
      prisonAppointments.addAll(chunk)
      offset += chunk.size
    } while (chunk.isNotEmpty())

    log.info("bookingId $bookingId: Retrieved ${prisonAppointments.size} VLB appointments from prison-api")

    return prisonAppointments
  }

  fun preAppointmentFinder(bookingId: Long, prisonAppointments: List<PrisonAppointment>): AppointmentFinder {
    val appointmentsByAppointmentId = videoLinkAppointmentRepository
      .unlinkedPreAppointmentsForBookingId(bookingId)
      .associateBy { it.appointmentId }

    val appointmentsByEndTime = prisonAppointments
      .filter { appointmentsByAppointmentId.containsKey(it.eventId) }
      .associateBy({ it.endTime }, { appointmentsByAppointmentId[it.eventId] })

    log.info("bookingId $bookingId: Found ${appointmentsByAppointmentId.size} PRE VideoLinkAppointments, Matched ${appointmentsByEndTime.size} with prison appointments")

    return ({ mainAppointment -> appointmentsByEndTime[mainAppointment?.startTime] })
  }

  fun postAppointmentFinder(bookingId: Long, prisonAppointments: List<PrisonAppointment>): AppointmentFinder {
    val appointmentsByAppointmentId = videoLinkAppointmentRepository
      .unlinkedPostAppointmentsForBookingId(bookingId)
      .associateBy { it.appointmentId }

    val appointmentsByStartTime = prisonAppointments
      .filter { appointmentsByAppointmentId.containsKey(it.eventId) }
      .associateBy({ it.startTime }, { appointmentsByAppointmentId[it.eventId] })

    log.info("bookingId $bookingId: Found ${appointmentsByAppointmentId.size} PRE VideoLinkAppointments, Matched ${appointmentsByStartTime.size} with prison appointments")

    return ({ mainAppointment -> appointmentsByStartTime[mainAppointment?.endTime] })
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    const val CHUNK_SIZE = 200
  }
}
