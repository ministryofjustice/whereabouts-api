package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingEventService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptionsService
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

@WebMvcTest(VideoLinkBookingController::class)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class VideoLinkBookingControllerTest : TestController() {
  @MockBean
  lateinit var courtService: CourtService

  @MockBean
  lateinit var videoLinkBookingService: VideoLinkBookingService

  @MockBean
  lateinit var videoLinkBookingEventService: VideoLinkBookingEventService

  @MockBean
  lateinit var videoLinkBookingOptionsService: VideoLinkBookingOptionsService

  val videoLinkBookingResponse = VideoLinkBookingResponse(
    videoLinkBookingId = 1,
    bookingId = 100,
    agencyId = "MDI",
    comment = "any comment",
    court = "Test Court",
    courtId = "TSTCRT",
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
    fun `Accept a valid request, main appointment only with court and courtId`() {
      passWithJson(
        objectMapper.writeValueAsString(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "courtId" to "TSTCRT",
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
    fun `Accept a valid request, main appointment with court but no courtId`() {
      passWithJson(
        objectMapper.writeValueAsString(
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
    fun `Accept a valid request, main appointment with courtId but no court`() {
      passWithJson(
        objectMapper.writeValueAsString(
          mapOf(
            "bookingId" to 1,
            "courtId" to "TSTCRT",
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
        objectMapper.writeValueAsString(
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
      whenever(videoLinkBookingService.createVideoLinkBooking(any())).thenReturn(1L)

      mockMvc.perform(
        post("/court/video-link-bookings")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json)
      )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$").value(1))
    }

    private fun failWithJson(json: String, expectedStatus: Int) {
      whenever(videoLinkBookingService.createVideoLinkBooking(any())).thenReturn(1L)

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
      doThrow(EntityNotFoundException("Video link booking with id $bookingId not found")).whenever(
        videoLinkBookingService
      )
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
      whenever(videoLinkBookingService.getVideoLinkBooking(any())).thenReturn(videoLinkBookingResponse)

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
                "courtId": "TSTCRT",
                "comment": "New comment",
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

      verify(videoLinkBookingService).updateVideoLinkBooking(
        1L,
        VideoLinkBookingUpdateSpecification(
          courtId = "TSTCRT",
          comment = "New comment",
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
  inner class `Update a comment` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Can update`() {

      mockMvc.perform(
        put("/court/video-link-bookings/1/comment")
          .contentType(MediaType.TEXT_PLAIN)
          .content(
            "Some Content"
          )
      )
        .andExpect(status().isNoContent)

      verify(videoLinkBookingService).updateVideoLinkBookingComment(
        1L,
        "Some Content"
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Can clear comment`() {

      mockMvc.perform(
        put("/court/video-link-bookings/1/comment")
          .contentType(MediaType.TEXT_PLAIN)
      )
        .andExpect(status().isNoContent)

      verify(videoLinkBookingService).updateVideoLinkBookingComment(
        1L,
        null
      )
    }
  }

  @Nested
  inner class `Deleting a booking` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `successful deletion`() {
      val bookingId = 1L
      whenever(videoLinkBookingService.deleteVideoLinkBooking(bookingId))
        .thenReturn(
          VideoLinkBooking(
            id = 1L,
            offenderBookingId = 2L,
            madeByTheCourt = true,
            courtId = "CRT",
            agencyId = "WWI",
            comment = "some comment"
          ).apply {
            addMainAppointment(3L, 10L, LocalDateTime.of(2022, 1, 1, 10, 0, 0), LocalDateTime.of(2022, 1, 1, 11, 0, 0))
          }
        )

      mockMvc.perform(
        delete("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().isNoContent)

      verify(videoLinkBookingService).deleteVideoLinkBooking(1)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `the booking does not exist`() {
      val bookingId = 1L
      doThrow(EntityNotFoundException("Video link booking with id $bookingId not found")).whenever(
        videoLinkBookingService
      )
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
  inner class GetBookingsByPrisonAndDateAndCourt {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get bookings on date`() {
      whenever(videoLinkBookingService.getVideoLinkBookingsForPrisonAndDateAndCourt(any(), any(), isNull(), isNull()))
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
                  "courtId": "TSTCRT",
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

      verify(videoLinkBookingService)
        .getVideoLinkBookingsForPrisonAndDateAndCourt("LEI", LocalDate.of(2020, 12, 25), null, null)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get bookings on date for court`() {
      whenever(
        videoLinkBookingService.getVideoLinkBookingsForPrisonAndDateAndCourt(
          any(),
          any(),
          anyString(),
          isNull()
        )
      )
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

      verify(videoLinkBookingService)
        .getVideoLinkBookingsForPrisonAndDateAndCourt("LEI", LocalDate.of(2020, 12, 25), "The Court", null)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get bookings on date for courtId`() {
      whenever(
        videoLinkBookingService.getVideoLinkBookingsForPrisonAndDateAndCourt(
          any(),
          any(),
          isNull(),
          anyString()
        )
      )
        .thenReturn(listOf())

      mockMvc.perform(
        get("/court/video-link-bookings/prison/LEI/date/2020-12-25?courtId=TSTCRT").accept(MediaType.APPLICATION_JSON)
      )
        .andExpect(
          matchAll(
            status().isOk,
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
          )
        )

      verify(videoLinkBookingService)
        .getVideoLinkBookingsForPrisonAndDateAndCourt("LEI", LocalDate.of(2020, 12, 25), null, "TSTCRT")
    }
  }

  @Nested
  inner class `Get videolink bookings by start date` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `happy flow`() {
      whenever(videoLinkBookingEventService.getBookingsByStartDateAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

      mockMvc
        .perform(
          get("/court/video-link-bookings")
            .param("start-date", "2021-03-01")
            .accept("text/csv")
        )
        .andExpect(status().isOk)
        .andExpect(content().contentType("text/csv;charset=UTF-8"))
        .andExpect(content().string(",,,,,,,,\n"))

      verify(videoLinkBookingEventService).getBookingsByStartDateAsCSV(LocalDate.of(2021, 3, 1), 7L)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `bad start-date`() {
      whenever(videoLinkBookingEventService.getBookingsByStartDateAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

      mockMvc
        .perform(
          get("/court/video-link-bookings")
            .param("start-date", "xxxxx")
            .accept("text/csv")
        )
        .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `missing start-date`() {
      whenever(videoLinkBookingEventService.getBookingsByStartDateAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

      mockMvc
        .perform(
          get("/court/video-link-bookings")
            .accept("text/csv")
        )
        .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `with days`() {
      whenever(videoLinkBookingEventService.getBookingsByStartDateAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

      mockMvc
        .perform(
          get("/court/video-link-bookings")
            .param("start-date", "2021-03-01")
            .param("days", "3")
            .accept("text/csv")
        )
        .andExpect(status().isOk)

      verify(videoLinkBookingEventService).getBookingsByStartDateAsCSV(LocalDate.of(2021, 3, 1), 3L)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `bad days`() {
      whenever(videoLinkBookingEventService.getBookingsByStartDateAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

      mockMvc
        .perform(
          get("/court/video-link-bookings")
            .param("start-date", "2021-03-01")
            .param("days", "xxxx")
            .accept("text/csv")
        )
        .andExpect(status().isBadRequest)
    }
  }
}
