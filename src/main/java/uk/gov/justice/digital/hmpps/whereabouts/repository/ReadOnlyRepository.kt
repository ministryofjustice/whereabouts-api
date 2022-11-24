package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import java.util.Optional

@NoRepositoryBean
interface ReadOnlyRepository<T, ID> : Repository<T, ID> {
  fun findById(id: ID): Optional<T>?
  fun findAll(): Set<T>?
}
