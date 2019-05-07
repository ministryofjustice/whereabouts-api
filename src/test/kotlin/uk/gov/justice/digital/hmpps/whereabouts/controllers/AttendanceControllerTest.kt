package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import java.time.LocalDate

open class AttendanceControllerTest {
    val mvc: MockMvc
    val attendanceService: AttendanceService
    val gson: Gson = getGson()

    init {
        attendanceService = mock<AttendanceService>(AttendanceService::class.java)
        mvc = standaloneSetup(OffenderAttendanceController(attendanceService))
                .build()
    }

    @Test
    fun `post attendance and receive a http 203 status code`() {
        val attendance = mapOf(
                "prisonId" to "LEI",
                "bookingId" to 1,
                "eventId" to 2,
                "eventLocationId" to 2,
                "eventDate" to "2019-10-10",
                "period" to "AM",
                "paid" to true
        )

        mvc.perform(
                post("/attendance")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(Gson().toJson(attendance))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated)

        verify<AttendanceService>(attendanceService).updateOffenderAttendance(
                AttendanceDto.builder()
                        .prisonId("LEI").bookingId(1).eventId(2).eventLocationId(2)
                        .eventDate(LocalDate.of(2019, 10, 10)).period("AM").paid(true)
                        .build())

    }


    @Test
    fun `return a bad request when the required fields are missing`() {
        mvc.perform(
                post("/attendance")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(mapOf("prisonId" to "LEI")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `receive a list of attendance for a prison, location, date and period`() {
        val attendance = AttendanceDto.builder()
                .id(1L)
                .prisonId("LEI")
                .bookingId(1L).eventId(2L).eventLocationId(2L)
                .period(TimePeriod.AM.toString()).paid(true)
                .eventDate(LocalDate.of(2019, 10, 8))
                .build()

        Mockito.`when`(attendanceService!!.getAttendance(
                "LEI",
                2L,
                LocalDate.of(2019, 10, 10),
                TimePeriod.AM))
                .thenReturn(mutableSetOf(attendance))

        val result = mvc.perform(
                get("/attendance/LEI/2?date=2019-10-10&period=AM").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andReturn()

        val response = result.response
        val json = response.contentAsString
        val attendanceResult: List<AttendanceDto> = gson.fromJson(json, object: TypeToken<List<AttendanceDto>>() {}.type)

        assertThat(attendanceResult).containsExactly(attendance)
    }
}
