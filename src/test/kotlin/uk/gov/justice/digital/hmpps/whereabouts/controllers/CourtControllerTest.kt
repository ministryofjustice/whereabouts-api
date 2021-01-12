package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.ResultMatcher.matchAll
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkAppointmentLinker
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AppointmentLocationsService
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AppointmentLocationsSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AvailableLocations
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.Interval
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.Location
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.EntityNotFoundException

@WebMvcTest(CourtController::class)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class CourtControllerTest : TestController() {
  @MockBean
  lateinit var courtService: CourtService

  @MockBean
  lateinit var videoLinkAppointmentLinker: VideoLinkAppointmentLinker

  @MockBean
  lateinit var appointmentLocationsService: AppointmentLocationsService

  val videoLinkBookingResponse = VideoLinkBookingResponse(
    videoLinkBookingId = 1,
    bookingId = 100,
    agencyId = "MDI",
    comment = "any comment",
    court = "Test Court",
    pre = VideoLinkBookingResponse.LocationTimeslot(
      locationId = 10,
      startTime = LocalDateTime.of(2020, 2, 7, 12, 0),
      endTime = LocalDateTime.of(2020, 2, 7, 13, 0),
    ),
    main = VideoLinkBookingResponse.LocationTimeslot(
      locationId = 9,
      startTime = LocalDateTime.of(2020, 2, 7, 13, 0),
      endTime = LocalDateTime.of(2020, 2, 7, 14, 0),
    ),
    post = VideoLinkBookingResponse.LocationTimeslot(
      locationId = 5,
      startTime = LocalDateTime.of(2020, 2, 7, 14, 0),
      endTime = LocalDateTime.of(2020, 2, 7, 15, 0),
    )
  )

  @Nested
  inner class `Creating a booking` {

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Accept a valid request, main appointment only`() {
      passWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 2,
              "startTime" to "2020-12-01T09:00",
              "endTime" to "2020-12-01T09:30"
            )
          )
        )
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Accept a valid request, pre, main and post`() {
      passWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "comment" to "Comment",
            "madeByTheCourt" to false,
            "pre" to mapOf(
              "locationId" to 1,
              "startTime" to "2020-12-01T09:00",
              "endTime" to "2020-12-01T09:30"
            ),
            "main" to mapOf(
              "locationId" to 2,
              "startTime" to "2020-12-01T09:30",
              "endTime" to "2020-12-01T10:00"
            ),
            "post" to mapOf(
              "locationId" to 3,
              "startTime" to "2020-12-01T10:00",
              "endTime" to "2020-12-01T10:30"
            )
          )
        )
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Missing madeByTheCourt doesn't fail`() {
      passWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "main" to mapOf(
              "locationId" to 2,
              "startTime" to "2020-12-01T09:00",
              "endTime" to "2020-12-01T09:30"
            )
          )
        )
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject missing main`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1"
          )
        ),
        400
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject missing bookingId`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "startTime" to "2020-12-01T09:30",
              "endTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject missing locationId`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "startTime" to "2020-12-01T09:30",
              "endTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject missing startTime`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 1,
              "endTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject bad startTime`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 1,
              "startTime" to "2020-13-45T00:00",
              "endTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject missing endTime`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 1,
              "startTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    fun `Valid request, no user`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 2,
              "startTime" to "2020-12-01T09:00",
              "endTime" to "2020-12-01T09:30"
            )
          )
        ),
        401
      )
    }

    private fun passWithJson(json: String) {
      whenever(courtService.createVideoLinkBooking(any())).thenReturn(1L)

      mockMvc.perform(
        post("/court/video-link-bookings")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json)
      )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$").value(1))
    }

    private fun failWithJson(json: String, expectedStatus: Int) {
      whenever(courtService.createVideoLinkBooking(any())).thenReturn(1L)

      mockMvc.perform(
        post("/court/video-link-bookings")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json)
      )
        .andExpect(status().`is`(expectedStatus))
    }
  }

  @Nested
  inner class `Get a booking` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `the booking does not exist`() {
      val bookingId = 1L
      doThrow(EntityNotFoundException("Video link booking with id $bookingId not found")).whenever(courtService)
        .getVideoLinkBooking(bookingId)

      mockMvc.perform(
        get("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().`is`(404))
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `the booking does exist`() {
      val bookingId = 1L
      whenever(courtService.getVideoLinkBooking(any())).thenReturn(videoLinkBookingResponse)

      mockMvc.perform(
        get("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.videoLinkBookingId").value(1))
        .andExpect(jsonPath("$.bookingId").value(100))
        .andExpect(jsonPath("$.comment").value("any comment"))
        .andExpect(jsonPath("$.court").value("Test Court"))
        .andExpect(jsonPath("$.pre.locationId").value(10))
        .andExpect(jsonPath("$.pre.startTime").value("2020-02-07T12:00:00"))
        .andExpect(jsonPath("$.pre.endTime").value("2020-02-07T13:00:00"))
        .andExpect(jsonPath("$.main.locationId").value(9))
        .andExpect(jsonPath("$.main.startTime").value("2020-02-07T13:00:00"))
        .andExpect(jsonPath("$.main.endTime").value("2020-02-07T14:00:00"))
        .andExpect(jsonPath("$.post.locationId").value(5))
        .andExpect(jsonPath("$.post.startTime").value("2020-02-07T14:00:00"))
        .andExpect(jsonPath("$.post.endTime").value("2020-02-07T15:00:00"))
    }

    @Test
    fun `there is no user`() {
      val bookingId = 1L

      mockMvc.perform(
        get("/court/video-link-bookings/$bookingId")
          .accept(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().isUnauthorized)
    }
  }

  @Nested
  inner class `Update a Booking` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Happy flow`() {

      mockMvc.perform(
        put("/court/video-link-bookings/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
              {
                "comment": "New comment",
                "madeByTheCourt": false,
                "pre": {
                  "locationId" : 1,
                  "startTime" : "2020-01-01T12:00",
                  "endTime": "2020-01-01T12:30"
                },
                "main": {
                  "locationId" : 2,
                  "startTime" : "2020-01-01T13:00",
                  "endTime": "2020-01-01T13:30"
                },
                "post": {
                  "locationId" : 3,
                  "startTime" : "2020-01-01T14:00",
                  "endTime": "2020-01-01T14:30"
                }
              }
            """
          )
      )
        .andExpect(status().isNoContent)

      verify(courtService).updateVideoLinkBooking(
        1L,
        VideoLinkBookingUpdateSpecification(
          comment = "New comment",
          madeByTheCourt = false,
          pre = VideoLinkAppointmentSpecification(
            locationId = 1L,
            startTime = LocalDateTime.of(2020, 1, 1, 12, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 12, 30)
          ),
          main = VideoLinkAppointmentSpecification(
            locationId = 2L,
            startTime = LocalDateTime.of(2020, 1, 1, 13, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 13, 30)

          ),
          post = VideoLinkAppointmentSpecification(
            locationId = 3L,
            startTime = LocalDateTime.of(2020, 1, 1, 14, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 14, 30)
          )
        )
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Bad request`() {

      mockMvc.perform(
        put("/court/video-link-bookings/X")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}")
      )
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class `Deleting a booking` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `successful deletion`() {
      val bookingId = 1L
      mockMvc.perform(
        delete("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().isNoContent)

      verify(courtService).deleteVideoLinkBooking(1)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `the booking does not exist`() {
      val bookingId = 1L
      doThrow(EntityNotFoundException("Video link booking with id $bookingId not found")).whenever(courtService)
        .deleteVideoLinkBooking(bookingId)

      mockMvc.perform(
        delete("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().`is`(404))
    }

    @Test
    fun `there is no user`() {
      val bookingId = 1L

      mockMvc.perform(
        delete("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().`is`(401))
    }
  }

  @Nested
  inner class GetBookingsByDateAndCourt {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get bookings on date`() {
      whenever(courtService.getVideoLinkBookingsForPrisonAndDateAndCourt(any(), any(), isNull()))
        .thenReturn(listOf(videoLinkBookingResponse))

      mockMvc.perform(
        get("/court/video-link-bookings/date/2020-12-25").accept(MediaType.APPLICATION_JSON)
      )
        .andExpect(
          matchAll(
            status().isOk,
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
            content().json(
              """
              [
                {
                  "videoLinkBookingId": 1,
                  "bookingId": 100,
                  "comment": "any comment",
                  "court": "Test Court",
                  "agencyId": "MDI",
                  "pre": {
                    "locationId": 10,
                    "startTime": "2020-02-07T12:00:00",
                    "endTime": "2020-02-07T13:00:00"
                  },
                  "main": {
                    "locationId": 9,
                    "startTime": "2020-02-07T13:00:00",
                    "endTime": "2020-02-07T14:00:00"
                  },
                  "post": {
                    "locationId": 5,
                    "startTime": "2020-02-07T14:00:00",
                    "endTime": "2020-02-07T15:00:00"
                  }
                }
              ]"""
            )
          )
        )

      verify(courtService)
        .getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", LocalDate.of(2020, 12, 25), null)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get bookings on date for court`() {
      whenever(courtService.getVideoLinkBookingsForPrisonAndDateAndCourt(any(), any(), anyString()))
        .thenReturn(listOf())

      mockMvc.perform(
        get("/court/video-link-bookings/date/2020-12-25?court=The Court").accept(MediaType.APPLICATION_JSON)
      )
        .andExpect(
          matchAll(
            status().isOk,
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
          )
        )

      verify(courtService)
        .getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", LocalDate.of(2020, 12, 25), "The Court")
    }
  }

  @Nested
  inner class GetBookingsByPrisonAndDateAndCourt {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get bookings on date`() {
      whenever(courtService.getVideoLinkBookingsForPrisonAndDateAndCourt(any(), any(), isNull()))
        .thenReturn(listOf(videoLinkBookingResponse.copy(agencyId = "LEI")))

      mockMvc.perform(
        get("/court/video-link-bookings/prison/LEI/date/2020-12-25").accept(MediaType.APPLICATION_JSON)
      )
        .andExpect(
          matchAll(
            status().isOk,
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
            content().json(
              """
              [
                {
                  "videoLinkBookingId": 1,
                  "bookingId": 100,
                  "comment": "any comment",
                  "court": "Test Court",
                  "agencyId": "LEI",
                  "pre": {
                    "locationId": 10,
                    "startTime": "2020-02-07T12:00:00",
                    "endTime": "2020-02-07T13:00:00"
                  },
                  "main": {
                    "locationId": 9,
                    "startTime": "2020-02-07T13:00:00",
                    "endTime": "2020-02-07T14:00:00"
                  },
                  "post": {
                    "locationId": 5,
                    "startTime": "2020-02-07T14:00:00",
                    "endTime": "2020-02-07T15:00:00"
                  }
                }
              ]"""
            )
          )
        )

      verify(courtService)
        .getVideoLinkBookingsForPrisonAndDateAndCourt("LEI", LocalDate.of(2020, 12, 25), null)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get bookings on date for court`() {
      whenever(courtService.getVideoLinkBookingsForPrisonAndDateAndCourt(any(), any(), anyString()))
        .thenReturn(listOf())

      mockMvc.perform(
        get("/court/video-link-bookings/prison/LEI/date/2020-12-25?court=The Court").accept(MediaType.APPLICATION_JSON)
      )
        .andExpect(
          matchAll(
            status().isOk,
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
          )
        )

      verify(courtService)
        .getVideoLinkBookingsForPrisonAndDateAndCourt("LEI", LocalDate.of(2020, 12, 25), "The Court")
    }
  }

  @Nested
  inner class FindLocationsForAppointmentIntervals {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `happy flow`() {
      whenever(appointmentLocationsService.findLocationsForAppointmentIntervals(any()))
        .thenReturn(
          listOf(
            AvailableLocations(
              Interval(LocalTime.of(9, 30), LocalTime.of(10, 0)),
              listOf(
                Location(1L, "Location 1")
              )
            ),
            AvailableLocations(
              Interval(LocalTime.of(12, 30), LocalTime.of(13, 0)),
              listOf(
                Location(2L, "Location 2"),
                Location(3L, "Location 3")
              )
            )
          )
        )

      mockMvc.perform(
        post("/court/appointment-location-finder")
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "date": "2020-02-10",
              "agencyId": "WWI",
              "vlbIdsToExclude": [1, 2, 3],
              "appointmentIntervals": [
                { "start": "09:30", "end": "10:00" },
                { "start": "12:30", "end": "13:00" }
              ]
            }
            """
          )
      )
        .andExpect(
          matchAll(
            status().isOk,
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
            content().json(
              """
                [
                  {
                    "appointmentInterval": { "start": "09:30:00", "end": "10:00:00" },
                    "locations": [
                      { "locationId": 1, "description": "Location 1" }
                    ]
                  },
                  {
                    "appointmentInterval": { "start": "12:30:00", "end": "13:00:00" },
                    "locations": [
                      { "locationId": 2, "description": "Location 2" },
                      { "locationId": 3, "description": "Location 3" }
                    ]
                  }
                ]
                """
            )
          )
        )

      verify(appointmentLocationsService).findLocationsForAppointmentIntervals(
        AppointmentLocationsSpecification(
          date = LocalDate.of(2020, 2, 10),
          agencyId = "WWI",
          vlbIdsToExclude = listOf(1L, 2L, 3L),
          appointmentIntervals = listOf(
            Interval(LocalTime.of(9, 30), LocalTime.of(10, 0)),
            Interval(LocalTime.of(12, 30), LocalTime.of(13, 0)),
          )
        )
      )
    }
  }
}
