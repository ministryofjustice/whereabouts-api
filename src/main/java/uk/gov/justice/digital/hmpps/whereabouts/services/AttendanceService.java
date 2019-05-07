package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.AbsentReasonsRepository;
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private AttendanceRepository attendanceRepository;
    private AbsentReasonsRepository absentReasonsRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, AbsentReasonsRepository absentReasonsRepository) {
        this.attendanceRepository = attendanceRepository;
        this.absentReasonsRepository = absentReasonsRepository;
    }

    public void updateOffenderAttendance(AttendanceDto updatedAttendance) {
            final var attendance = Attendance
                    .builder()
                    .eventLocationId(updatedAttendance.getEventLocationId())
                    .eventDate(updatedAttendance.getEventDate())
                    .eventId(updatedAttendance.getEventId())
                    .offenderBookingId(updatedAttendance.getBookingId())
                    .period(TimePeriod.valueOf(updatedAttendance.getPeriod()))
                    .paid(updatedAttendance.isPaid())
                    .attended(updatedAttendance.isAttended())
                    .prisonId(updatedAttendance.getPrisonId())
                    .absentReasonId(updatedAttendance.getAbsentReasonId())
                    .build();

            attendanceRepository.save(attendance);
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
                        .period(String.valueOf(attendanceData.getPeriod()))
                        .paid(attendanceData.isPaid())
                        .attended(attendanceData.isAttended())
                        .prisonId(attendanceData.getPrisonId())
                        .absentReasonId(attendanceData.getAbsentReasonId())
                        .eventLocationId(attendanceData.getEventLocationId())
                        .build())
                  .collect(Collectors.toSet());
    }

    public List<AbsentReasonDto> getAbsentReasons() {
       return absentReasonsRepository.findAll()
                .stream()
                .map(reason -> AbsentReasonDto.builder()
                        .id(reason.getId())
                        .paidReason(reason.isPaidReason())
                        .reason(reason.getReason())
                        .build())
                .sorted(Comparator.comparing(AbsentReasonDto::getReason))
                .collect(Collectors.toList());
    }
}
