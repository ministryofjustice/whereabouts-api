package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.NotifyRequest
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@Service
class NotifyService(
  @Value("\${notify.enabled}") private val enabled: Boolean,
  @Value("\${notify.templates.prison.transferred}") private val offenderTransferredPrisonTemplateId: String,
  @Value("\${notify.templates.court.transferred}") private val offenderTransferredCourtTemplateId: String,
  @Value("\${notify.templates.prison.released}") private val offenderReleasedPrisonTemplateId: String,
  @Value("\${notify.templates.court.released}") private val offenderReleasedCourtTemplateId: String,
  private val client: NotificationClient,
) {

  fun sendOffenderTransferredEmailToPrisonOnly(emailData: NotifyRequest) {
  }

  fun sendOffenderReleasedEmail(emailData: NotifyRequest, courtName: String, courtEmail: String) {
  }

  fun sendOffenderReleasedEmailToPrisonOnly(emailData: NotifyRequest) {
  }

  fun sendOffenderTransferredEmail(
    notifyRequest: NotifyRequest,
    courtName: String,
    courtEmail: String,
  ) {
    if (enabled) {
      val values: Map<String, String> = mapOf(
        "hearingLocation" to courtName,
        "prison" to notifyRequest.prisonName,
        "firstName" to notifyRequest.firstName,
        "lastName" to notifyRequest.lastName,
        "dateOfBirth" to notifyRequest.dateOfBirth,
        "offenderId" to notifyRequest.offenderId,
        "date" to notifyRequest.mainAppointmentDate,
        "startTime" to notifyRequest.mainAppointmentStartTime,
        "endTime" to notifyRequest.mainAppointmentEndTime,
        "preHearingStartTime" to notifyRequest.preHearingStartTime,
        "preHearingEndTime" to notifyRequest.preHearingEndTime,
        "postHearingStartTime" to notifyRequest.preHearingStartTime,
        "postHearingEndTime" to notifyRequest.preHearingEndTime,
        "comments" to notifyRequest.comments,
      )

      if (notifyRequest.prisonEmail == null) {
        log.error("Notification failed - prison email address not present for BVL appointment with bookingId  ${notifyRequest.videoLinkBookingId}")
      } else if (courtEmail == null) {
        log.error("Notification failed - court email address not present for BVL appointment with bookingId  ${notifyRequest.videoLinkBookingId}")
      }

      if (notifyRequest.prisonEmail != null) {
        sendEmail(offenderTransferredPrisonTemplateId, notifyRequest.prisonEmail, values, null)
        log.info(
          "BVL appointment cancellation following offender release. " +
            "Email sent to recipient: ${notifyRequest.prisonEmail}, " +
            "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
            "offender Noms number: ${values["offenderId"]}, " +
            "BVL booking id: ${values["videoLinkBookingId"]} ",
        )
      } else if (courtEmail != null) {
        sendEmail(offenderTransferredCourtTemplateId, courtEmail, values, null)
        log.info(
          "BVL appointment cancellation following offender release. " +
            "Email sent to recipient: $courtEmail, " +
            "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
            "offender Noms number: ${values["offenderId"]}, " +
            "BVL booking id: ${values["videoLinkBookingId"]} ",
        )
      }
    }
  }

  private fun sendEmail(templateId: String, emailAddress: String, values: Map<String, Any>, reference: String?) {
    if (!enabled) {
      log.info("Notification disabled: Did not send notification to $emailAddress for $templateId ref $reference")
      return
    }

    try {
      client.sendEmail(templateId, emailAddress, values, reference)
    } catch (e: NotificationClientException) {
      log.error("Notification failed - templateId $templateId to $emailAddress", e)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(NotifyService::class.java)
  }
}
