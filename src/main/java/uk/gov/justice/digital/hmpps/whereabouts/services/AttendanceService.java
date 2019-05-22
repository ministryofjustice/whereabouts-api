package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonsDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AttendanceService {
    private AttendanceRepository attendanceRepository;
    private NomisService nomisService;
    private NomisEventOutcomeMap nomisEventOutcomeMap = new NomisEventOutcomeMap();

    public AttendanceService(final AttendanceRepository attendanceRepository, final NomisService nomisService) {
        this.attendanceRepository = attendanceRepository;
        this.nomisService = nomisService;
    }

    @Transactional
    public void createOffenderAttendance(CreateAttendanceDto updatedAttendance) {
        attendanceRepository.save(Attendance
                .builder()
                .eventLocationId(updatedAttendance.getEventLocationId())
                .eventDate(updatedAttendance.getEventDate())
                .eventId(updatedAttendance.getEventId())
                .offenderBookingId(updatedAttendance.getBookingId())
                .period(updatedAttendance.getPeriod())
                .paid(updatedAttendance.isPaid())
                .attended(updatedAttendance.isAttended())
                .prisonId(updatedAttendance.getPrisonId())
                .absentReason(updatedAttendance.getAbsentReason())
                .comments(updatedAttendance.getComments())
                .build());

        if (updatedAttendance.getAbsentReason() == AbsentReason.Refused ||
                updatedAttendance.getAbsentReason() == AbsentReason.UnacceptableAbsence) {

            final var comments = updatedAttendance.getComments() != null  ?
                    updatedAttendance.getComments() :
                    "Refused to attend activity / education.";

            nomisService.postCaseNote(
                    updatedAttendance.getBookingId(),
                    "NEG",//"Negative Behaviour"
                    "IEP_WARN", //"IEP Warning",
                    comments,
                    LocalDateTime.now()
            );
        }

        final var nomisCodes = nomisEventOutcomeMap.getEventOutCome(
                updatedAttendance.getAbsentReason(),
                updatedAttendance.isAttended(),
                updatedAttendance.isPaid());

        nomisService.updateAttendance(
                updatedAttendance.getOffenderNo(),
                updatedAttendance.getEventId(),
                nomisCodes.getOutcome(),
                nomisCodes.getPerformance()
        );

    }

    public Set<AttendanceDto> getAttendance(String prisonId, Long eventLocationId, LocalDate date, TimePeriod period) {
        final var attendance = attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod(prisonId, eventLocationId, date, period);

        return attendance
                .stream()
                .map(attendanceData -> AttendanceDto.builder()
                        .id(attendanceData.getId())
                        .eventDate(attendanceData.getEventDate())
                        .eventId(attendanceData.getEventId())
                        .bookingId(attendanceData.getOffenderBookingId())
                        .period(attendanceData.getPeriod())
                        .paid(attendanceData.isPaid())
                        .attended(attendanceData.isAttended())
                        .prisonId(attendanceData.getPrisonId())
                        .absentReason(attendanceData.getAbsentReason())
                        .eventLocationId(attendanceData.getEventLocationId())
                        .comments(attendanceData.getComments())
                        .build())
                  .collect(Collectors.toSet());
    }

    public AbsentReasonsDto getAbsenceReasons() {
        final var paidReasons = Set.of(
                AbsentReason.AcceptableAbsence,
                AbsentReason.NotRequired
        );
        final var unpaidReasons = Set.of(
                AbsentReason.SessionCancelled,
                AbsentReason.RestInCell,
                AbsentReason.RestDay,
                AbsentReason.UnacceptableAbsence,
                AbsentReason.Refused,
                AbsentReason.Sick
        );

        return new AbsentReasonsDto(paidReasons, unpaidReasons);
    }
}
