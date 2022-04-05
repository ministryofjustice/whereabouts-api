package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.whereabouts.model.Court
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtRepository

class CourtServiceTest {

  val repository: CourtRepository = mock()

  val DERBY_JC_ID = "DRBYJC"
  val DERBY_JC_NAME = "Derby Justice Centre"
  val DERBY_JC_EMAIL = "derby@derby.org"

  val courts = listOf(
    Court(DERBY_JC_ID, DERBY_JC_NAME, DERBY_JC_EMAIL),
    Court("DUDLMC", "Dudley"),
    Court("HERFCC", "Hereford Crown"),

  )

  @BeforeEach
  fun initialiseRepository() {
    whenever(repository.findAll(isA<Sort>())).thenReturn(courts)
  }

  @Test
  fun `should cache courts`() {
    val service = CourtService(repository)
    val retrievedCourts = service.courts

    assertThat(retrievedCourts).isEqualTo(courts)

    verify(repository).findAll(Sort.by("name"))

    service.courts
    verifyNoMoreInteractions(repository)
  }

  @Test
  fun `courtNames`() {
    val service = CourtService(repository)
    assertThat(service.courtNames).containsExactly(DERBY_JC_NAME, "Dudley", "Hereford Crown")
  }

  @Nested
  inner class FindName {
    @Test
    fun `found`() {
      assertThat(CourtService(repository).getCourtNameForCourtId(DERBY_JC_ID)).isEqualTo(DERBY_JC_NAME)
    }

    @Test
    fun `Not Found`() {
      assertThat(CourtService(repository).getCourtNameForCourtId("XXXX")).isNull()
    }
  }

  @Nested
  inner class FindEmail {
    @Test
    fun `found`() {
      assertThat(CourtService(repository).getCourtEmailForCourtId(DERBY_JC_ID)).isEqualTo(DERBY_JC_EMAIL)
    }

    @Test
    fun `Not Found`() {
      assertThat(CourtService(repository).getCourtEmailForCourtId("XXXX")).isNull()
    }
  }

  @Nested
  inner class FindId {
    @Test
    fun `perfect match`() {
      assertThat(CourtService(repository).getCourtIdForCourtName(DERBY_JC_NAME)).isEqualTo(DERBY_JC_ID)
    }

    @Test
    fun `case insensitive match with whitespace`() {
      assertThat(CourtService(repository).getCourtIdForCourtName(" ${DERBY_JC_NAME.uppercase()}  ")).isEqualTo(
        DERBY_JC_ID
      )
    }
  }
}
