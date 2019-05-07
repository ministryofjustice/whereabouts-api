package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
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
                    .absentReason(updatedAttendance.getAbsentReason())
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
                        .absentReason(attendanceData.getAbsentReason())
                        .eventLocationId(attendanceData.getEventLocationId())
                        .build())
                  .collect(Collectors.toSet());
    }
}
