package uk.gov.justice.digital.hmpps.whereabouts.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.*;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository;
import uk.gov.justice.digital.hmpps.whereabouts.utils.AbsentReasonFormatter;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Slf4j
@AllArgsConstructor
public class AttendanceService {
    private final static NomisEventOutcomeMapper nomisEventOutcomeMapper = new NomisEventOutcomeMapper();

    private final AttendanceRepository attendanceRepository;
    private final Elite2ApiService elite2ApiService;
    private final CaseNotesService caseNotesService;

    public Set<AttendanceDto> getAttendanceForEventLocation(final String prisonId, final Long eventLocationId, final LocalDate date, final TimePeriod period) {
        final var attendance = attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(prisonId, eventLocationId, date, period);

        return attendance
                .stream()
                .map(this::toAttendanceDto)
                .collect(Collectors.toSet());
    }

    public Set<AttendanceDto> getAbsences(final String prisonId, final LocalDate date, final TimePeriod period) {
        final var attendance = attendanceRepository
                .findByPrisonIdAndEventDateAndPeriodAndAbsentReasonNotNull(prisonId, date, period);

        return attendance
                .stream()
                .map(this::toAttendanceDto)
                .collect(Collectors.toSet());
    }

    public Set<AttendanceDto> getAttendanceForBookings(final String prisonId, final Set<Long> bookings, final LocalDate date, final TimePeriod period) {
        final var attendance = attendanceRepository
                .findByPrisonIdAndBookingIdInAndEventDateAndPeriod(prisonId, bookings, date, period);

        return attendance
                .stream()
                .map(this::toAttendanceDto)
                .collect(Collectors.toSet());
    }

    public AbsentReasonsDto getAbsenceReasons() {
        return new AbsentReasonsDto(
                AbsentReason.getPaidReasons(),
                AbsentReason.getUnpaidReasons(),
                AbsentReason.getIepTriggers()
        );
    }

    @Transactional
    public AttendanceDto createAttendance(final CreateAttendanceDto attendanceDto) {

        final var attendance = attendanceRepository.save(toAttendance(attendanceDto));

        postNomisAttendance(attendance);
        postIEPWarningIfRequired(
                attendance.getBookingId(),
                attendance.getCaseNoteId(),
                attendance.getAbsentReason(),
                attendance.getComments()
        ).ifPresent(attendance::setCaseNoteId);

        log.info("attendance created {}", attendance.toBuilder().comments(null));

        return toAttendanceDto(attendance);
    }

    @Transactional
    public void updateAttendance(final long id, final UpdateAttendanceDto newAttendanceDetails) throws AttendanceNotFound, AttendanceLocked {
        final var attendance = attendanceRepository.findById(id).orElseThrow(AttendanceNotFound::new);

        if (isAttendanceLocked(attendance)) {
            log.info("Update attempted on locked attendance, attendance id {}", id);
            throw new AttendanceLocked();
        }

        handleIEPWarningScenarios(attendance, newAttendanceDetails).ifPresent(attendance::setCaseNoteId);

        attendance.setComments(newAttendanceDetails.getComments());
        attendance.setAttended(newAttendanceDetails.getAttended());
        attendance.setPaid(newAttendanceDetails.getPaid());
        attendance.setAbsentReason(newAttendanceDetails.getAbsentReason());

        attendanceRepository.save(attendance);

        postNomisAttendance(attendance);
    }

    @Transactional
    public Set<AttendanceDto> attendAll(final AttendAllDto attendAll) {
        final var eventOutcome = nomisEventOutcomeMapper.getEventOutcome(null, true, true, "");

        elite2ApiService.putAttendanceForMultipleBookings(attendAll.getBookingActivities(), eventOutcome);


        final var attendances =
                attendAll.getBookingActivities()
                        .stream()
                        .map(bookingActivity -> Attendance.builder()
                                .attended(true)
                                .paid(true)
                                .bookingId(bookingActivity.getBookingId())
                                .eventId(bookingActivity.getActivityId())
                                .eventDate(attendAll.getEventDate())
                                .eventLocationId(attendAll.getEventLocationId())
                                .period(attendAll.getPeriod())
                                .prisonId(attendAll.getPrisonId())
                                .build())
                        .collect(Collectors.toSet());

        attendanceRepository.saveAll(attendances);

        return attendances
                .stream()
                .map(this::toAttendanceDto)
                .collect(Collectors.toSet());
    }

    private Optional<Long> handleIEPWarningScenarios(final Attendance attendance, final UpdateAttendanceDto newAttendanceDetails) {
        final var alreadyTriggeredIEPWarning =
                attendance.getAbsentReason() != null &&
                        AbsentReason.getIepTriggers().contains(attendance.getAbsentReason());

        final var shouldTriggerIEPWarning =
                newAttendanceDetails.getAbsentReason() != null &&
                        AbsentReason.getIepTriggers().contains(newAttendanceDetails.getAbsentReason());

        if (alreadyTriggeredIEPWarning && shouldTriggerIEPWarning) return Optional.empty();

        final var formattedAbsentReason = newAttendanceDetails.getAbsentReason() != null ?
                AbsentReasonFormatter.titlecase(newAttendanceDetails.getAbsentReason().toString()) : null;

        final var shouldRevokePreviousIEPWarning = attendance.getCaseNoteId() != null && !shouldTriggerIEPWarning;

        final var shouldReinstatePreviousIEPWarning = attendance.getCaseNoteId() != null && shouldTriggerIEPWarning;

        if (shouldRevokePreviousIEPWarning) {

            final var rescindedReason = "IEP rescinded: " + (newAttendanceDetails.getAttended() ? "attended" : formattedAbsentReason);

            log.info("{} raised for {}", rescindedReason, attendance.toBuilder().comments(null));
            final var offenderNo = elite2ApiService.getOffenderNoFromBookingId(attendance.getBookingId());

            caseNotesService.putCaseNoteAmendment(offenderNo, attendance.getCaseNoteId(), rescindedReason);

            return Optional.empty();
        }
        if (shouldReinstatePreviousIEPWarning) {

            final var reinstatedReason = "IEP reinstated: " + formattedAbsentReason;

            log.info("{} raised for {}", reinstatedReason, attendance.toBuilder().comments(null));
            final var offenderNo = elite2ApiService.getOffenderNoFromBookingId(attendance.getBookingId());

            caseNotesService.putCaseNoteAmendment(offenderNo, attendance.getCaseNoteId(), reinstatedReason);

            return Optional.empty();
        }
        return postIEPWarningIfRequired(
                attendance.getBookingId(),
                attendance.getCaseNoteId(),
                newAttendanceDetails.getAbsentReason(),
                newAttendanceDetails.getComments()
        );
    }


    private void postNomisAttendance(final Attendance attendance) {
        final var eventOutcome = nomisEventOutcomeMapper.getEventOutcome(
                attendance.getAbsentReason(),
                attendance.getAttended(),
                attendance.getPaid(),
                attendance.getComments());

        log.info("Updating attendance on NOMIS {} {}", attendance.toBuilder().comments(null).build(), eventOutcome);

        elite2ApiService.putAttendance(attendance.getBookingId(), attendance.getEventId(), eventOutcome);
    }

    private Optional<Long> postIEPWarningIfRequired(final Long bookingId, final Long caseNoteId, final AbsentReason reason, final String text) {
        if (caseNoteId == null && reason != null && AbsentReason.getIepTriggers().contains(reason)) {
            log.info("IEP Warning created for bookingId {}", bookingId);
            final var offenderNo = elite2ApiService.getOffenderNoFromBookingId(bookingId);

            final var modifiedTextWithReason = AbsentReasonFormatter.titlecase(reason.toString()) + " - " + text;
            final var caseNote = caseNotesService.postCaseNote(
                    offenderNo,
                    "NEG",//"Negative Behaviour"
                    "IEP_WARN", //"IEP Warning",
                    modifiedTextWithReason,
                    LocalDateTime.now());
            return Optional.of(caseNote.getCaseNoteId());
        }

        return Optional.empty();
    }

    private Boolean isAttendanceLocked(final Attendance attendance) {
        if (attendance.getCreateDateTime() == null)
            return false;

        final var dateOfChange = attendance.getModifyDateTime() == null ?
                attendance.getCreateDateTime().toLocalDate() :
                attendance.getModifyDateTime().toLocalDate();

        final var dateDifference = DAYS.between(dateOfChange, LocalDate.now());

        return (attendance.getPaid()) ? dateDifference >= 1 : dateDifference >= 7;
    }

    private Attendance toAttendance(final CreateAttendanceDto attendanceDto) {
        return Attendance
                .builder()
                .eventLocationId(attendanceDto.getEventLocationId())
                .eventDate(attendanceDto.getEventDate())
                .eventId(attendanceDto.getEventId())
                .bookingId(attendanceDto.getBookingId())
                .period(attendanceDto.getPeriod())
                .paid(attendanceDto.getPaid())
                .attended(attendanceDto.getAttended())
                .prisonId(attendanceDto.getPrisonId())
                .absentReason(attendanceDto.getAbsentReason())
                .comments(attendanceDto.getComments())
                .build();
    }

    private AttendanceDto toAttendanceDto(final Attendance attendanceData) {
        return AttendanceDto.builder()
                .id(attendanceData.getId())
                .eventDate(attendanceData.getEventDate())
                .eventId(attendanceData.getEventId())
                .bookingId(attendanceData.getBookingId())
                .period(attendanceData.getPeriod())
                .paid(attendanceData.getPaid())
                .attended(attendanceData.getAttended())
                .prisonId(attendanceData.getPrisonId())
                .absentReason(attendanceData.getAbsentReason())
                .eventLocationId(attendanceData.getEventLocationId())
                .comments(attendanceData.getComments())
                .createUserId(attendanceData.getCreateUserId())
                .createDateTime(attendanceData.getCreateDateTime())
                .caseNoteId(attendanceData.getCaseNoteId())
                .locked(isAttendanceLocked(attendanceData))
                .modifyDateTime(attendanceData.getModifyDateTime())
                .modifyUserId(attendanceData.getModifyUserId())
                .build();
    }

    public Set<AttendanceDto> getAttendanceForOffendersThatHaveScheduledActivity(final String prisonId, final LocalDate date, final TimePeriod period) {
        final var bookingIds = elite2ApiService.getBookingIdsForScheduleActivities(prisonId, date, period);

        final var attendances = attendanceRepository.
                findByPrisonIdAndBookingIdInAndEventDateAndPeriod(prisonId, bookingIds, date, period);

        return attendances.stream().map(this::toAttendanceDto).collect(Collectors.toSet());
    }
}
