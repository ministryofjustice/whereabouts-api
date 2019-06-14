package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonsDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;


@Service
public class AttendanceService {
    private final static NomisEventOutcomeMapper nomisEventOutcomeMapper = new NomisEventOutcomeMapper();

    private final AttendanceRepository attendanceRepository;
    private final NomisService nomisService;


    public AttendanceService(final AttendanceRepository attendanceRepository, final NomisService nomisService) {
        this.attendanceRepository = attendanceRepository;
        this.nomisService = nomisService;
    }

    public Set<AttendanceDto> getAttendance(final String prisonId, final Long eventLocationId, final LocalDate date, final TimePeriod period) {
        final var attendance = attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(prisonId, eventLocationId, date, period);

        return attendance
                .stream()
                .map(this::toAttendanceDto)
                .collect(Collectors.toSet());
    }

    public AbsentReasonsDto getAbsenceReasons() {
        return new AbsentReasonsDto(AbsentReason.getPaidReasons(), AbsentReason.getUnpaidReasons());
    }

    @Transactional
    public AttendanceDto createAttendance(final CreateAttendanceDto attendanceDto) {

        var attendance = attendanceRepository.save(toAttendance(attendanceDto));

        postNomisAttendance(attendance);
        postIEPWarningIfRequired(
                attendance.getOffenderBookingId(),
                attendance.getCaseNoteId(),
                attendance.getAbsentReason(),
                attendance.getComments()
        ).ifPresent(attendance::setCaseNoteId);

        return toAttendanceDto(attendance);
    }

    @Transactional
    public void updateAttendance(final long id, final UpdateAttendanceDto newAttendanceDetails) throws AttendanceNotFound, AttendanceLocked {
        final var attendance = attendanceRepository.findById(id).orElseThrow(AttendanceNotFound::new);

        if (isAttendanceLocked(attendance.getPaid(), attendance.getEventDate())) {
            throw new AttendanceLocked();
        }

        final var shouldTriggerIEPWarning =
                newAttendanceDetails.getAbsentReason() != null &&
                        AbsentReason.getIepTriggers().contains(newAttendanceDetails.getAbsentReason());

        final var shouldRevokePreviousIEPWarning = attendance.getCaseNoteId() != null && !shouldTriggerIEPWarning;

        if (shouldRevokePreviousIEPWarning) {
            final var rescindedReason = "IEP rescinded: " + (newAttendanceDetails.getAttended() ?
                    "attended" : newAttendanceDetails.getAbsentReason().toString());
            nomisService.putCaseNoteAmendment(
                    attendance.getOffenderBookingId(),
                    attendance.getCaseNoteId(),
                    rescindedReason
            );
        } else {
            postIEPWarningIfRequired(
                    attendance.getOffenderBookingId(),
                    attendance.getCaseNoteId(),
                    newAttendanceDetails.getAbsentReason(),
                    newAttendanceDetails.getComments()
            ).ifPresent(attendance::setCaseNoteId);
        }

        attendance.setComments(newAttendanceDetails.getComments());
        attendance.setAttended(newAttendanceDetails.getAttended());
        attendance.setPaid(newAttendanceDetails.getPaid());
        attendance.setAbsentReason(newAttendanceDetails.getAbsentReason());

        attendanceRepository.save(attendance);

        postNomisAttendance(attendance);
    }


    private void postNomisAttendance(final Attendance attendance) {
        final var eventOutcome = nomisEventOutcomeMapper.getEventOutcome(
                attendance.getAbsentReason(),
                attendance.getAttended(),
                attendance.getPaid());

        nomisService.putAttendance(attendance.getOffenderBookingId(), attendance.getEventId(), eventOutcome);
    }

    private Optional<Long> postIEPWarningIfRequired(final Long bookingId, final Long caseNoteId, final AbsentReason reason, final String text) {
        if (caseNoteId == null && reason != null && AbsentReason.getIepTriggers().contains(reason)) {
            final var caseNote = nomisService.postCaseNote(
                    bookingId,
                    "NEG",//"Negative Behaviour"
                    "IEP_WARN", //"IEP Warning",
                    text,
                    LocalDateTime.now());
             return Optional.of(caseNote.getCaseNoteId());
        }

        return Optional.empty();
    }

    private Boolean isAttendanceLocked(final Boolean paid, final LocalDate eventDate) {
        final var dateDifference = DAYS.between(eventDate, LocalDate.now());

        return (paid) ? dateDifference >= 1 : dateDifference >= 7;
    }

    private Attendance toAttendance(final CreateAttendanceDto attendanceDto) {
         return Attendance
                 .builder()
                 .eventLocationId(attendanceDto.getEventLocationId())
                 .eventDate(attendanceDto.getEventDate())
                 .eventId(attendanceDto.getEventId())
                 .offenderBookingId(attendanceDto.getBookingId())
                 .period(attendanceDto.getPeriod())
                 .paid(attendanceDto.getPaid())
                 .attended(attendanceDto.getAttended())
                 .prisonId(attendanceDto.getPrisonId())
                 .absentReason(attendanceDto.getAbsentReason())
                 .comments(attendanceDto.getComments())
                 .build();
     }

     private AttendanceDto toAttendanceDto(Attendance attendanceData) {
        return AttendanceDto.builder()
                .id(attendanceData.getId())
                .eventDate(attendanceData.getEventDate())
                .eventId(attendanceData.getEventId())
                .bookingId(attendanceData.getOffenderBookingId())
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
                .locked(isAttendanceLocked(attendanceData.getPaid(), attendanceData.getEventDate()))
                .modifyDateTime(attendanceData.getModifyDateTime())
                .modifyUserId(attendanceData.getModifyUserId())
                .build();
     }
}
