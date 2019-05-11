package uk.gov.justice.digital.hmpps.whereabouts.controllers;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Set;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Configuration
class MyConfig extends WebMvcConfigurationSupport {
    @Override
    public FormattingConversionService mvcConversionService() {
        FormattingConversionService f = super.mvcConversionService();
        f.addConverter(new AbsentReasonConverter());
        return f;
    }
}

@Slf4j
class AbsentReasonConverter implements Converter<String, AbsentReason> {
    @Override
    public AbsentReason convert(String source) {
        log.info("SOURCE ============= " + source);
        try {
            return AbsentReason.valueOf(source);
        } catch (Exception e) {
            return null;
        }
    }
}

@Api(tags = {"attendance"})
@RestController()
@RequestMapping(
        value="attendance",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class OffenderAttendanceController {

    private AttendanceService attendanceService;

    public OffenderAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void updateAttendance(@RequestBody @Valid AttendanceDto attendance) {
        this.attendanceService.updateOffenderAttendance(attendance);
    }

    @GetMapping("/{prison}/{event-location}")
    public Set<AttendanceDto> getAttendance(@PathVariable("prison") String prisonId,
                                             @PathVariable("event-location") Long eventLocationId,
                                             @RequestParam @DateTimeFormat(iso= DATE) LocalDate date,
                                             @RequestParam String period) {

       return this.attendanceService.getAttendance(prisonId, eventLocationId, date, TimePeriod.valueOf(period));

    }

    @GetMapping("/absence-reasons")
    public AbsentReason[] reasons() {
        return AbsentReason.values();
    }
}
