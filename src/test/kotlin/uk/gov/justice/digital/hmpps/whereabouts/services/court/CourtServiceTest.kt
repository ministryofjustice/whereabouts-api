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

  private val repository: CourtRepository = mock()

  private val derbyJcId = "DRBYJC"
  private val derbyJcName = "Derby Justice Centre"
  private val derbyJcEmail = "derby@derby.org"

  private val courts = listOf(
    Court(derbyJcId, derbyJcName, derbyJcEmail),
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
    assertThat(service.courtNames).containsExactly(derbyJcName, "Dudley", "Hereford Crown")
  }

  @Nested
  inner class FindName {
    @Test
    fun `found`() {
      assertThat(CourtService(repository).getCourtNameForCourtId(derbyJcId)).isEqualTo(derbyJcName)
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
      assertThat(CourtService(repository).getCourtEmailForCourtId(derbyJcId)).isEqualTo(derbyJcEmail)
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
      assertThat(CourtService(repository).getCourtIdForCourtName(derbyJcName)).isEqualTo(derbyJcId)
    }

    @Test
    fun `case insensitive match with whitespace`() {
      assertThat(CourtService(repository).getCourtIdForCourtName(" ${derbyJcName.uppercase()}  ")).isEqualTo(
        derbyJcId,
      )
    }
  }
}
