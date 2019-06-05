package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.*;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AttendanceService {
    private final static NomisEventOutcomeMapper nomisEventOutcomeMapper = new NomisEventOutcomeMapper();

    private final AttendanceRepository attendanceRepository;
    private final NomisService nomisService;

    public AttendanceService(final AttendanceRepository attendanceRepository, final NomisService nomisService) {
        this.attendanceRepository = attendanceRepository;
        this.nomisService = nomisService;
    }

    @Transactional
    public void createAttendance(final CreateAttendanceDto attendanceDto) {
        final var attendance = toAttendance(attendanceDto);

        attendanceRepository.save(attendance);

        applyAttendanceWorkflow(attendanceDto.getOffenderNo(), attendance);
    }

    @Transactional
    public void updateAttendance(long id, UpdateAttendanceDto attendanceDto) throws AttendanceNotFound {

        final var attendance = attendanceRepository.findById(id)
                .orElseThrow(AttendanceNotFound::new);

        attendance.setAttended(attendanceDto.getAttended());
        attendance.setPaid(attendanceDto.getPaid());
        attendance.setAbsentReason(attendanceDto.getAbsentReason());
        attendance.setComments(attendanceDto.getComments());

        final BasicBookingDetails basicBookingDetails = nomisService.getBasicBookingDetails(attendance.getOffenderBookingId());

        attendanceRepository.save(attendance);

        applyAttendanceWorkflow(basicBookingDetails.getOffenderNo(), attendance);
    }



    public Set<AttendanceDto> getAttendance(final String prisonId, final Long eventLocationId, final LocalDate date, final TimePeriod period) {
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
                        .createUserId(attendanceData.getCreateUserId())
                        .createDateTime(attendanceData.getCreateDateTime())
                        .caseNoteId(attendanceData.getCaseNoteId())
                        .build())
                  .collect(Collectors.toSet());
    }

    public AbsentReasonsDto getAbsenceReasons() {
        return new AbsentReasonsDto(AbsentReason.getPaidReasons(), AbsentReason.getUnpaidReasons());
    }


    private void applyAttendanceWorkflow(String offenderNo, Attendance attendance) {
         final var eventOutcome = nomisEventOutcomeMapper.getEventOutcome(
                 attendance.getAbsentReason(),
                 attendance.isAttended(),
                 attendance.isPaid());

         nomisService.putAttendance(offenderNo, attendance.getEventId(), eventOutcome);

         if (attendance.getAbsentReason() != null && AbsentReason.getIepTriggers().contains(attendance.getAbsentReason())) {
             final var caseNote = nomisService.postCaseNote(
                     attendance.getOffenderBookingId(),
                     "NEG",//"Negative Behaviour"
                     "IEP_WARN", //"IEP Warning",
                     attendance.getComments(),
                     LocalDateTime.now());

             attendance.setCaseNoteId(caseNote.getCaseNoteId());
         }
     }

     private Attendance toAttendance(CreateAttendanceDto attendanceDto) {
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
}
