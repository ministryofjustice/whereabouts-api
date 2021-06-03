package uk.gov.justice.digital.hmpps.whereabouts.model

import org.hibernate.Hibernate
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
class BaseEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as BaseEntity
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1004284837
}
