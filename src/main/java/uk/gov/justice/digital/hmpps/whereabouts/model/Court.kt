package uk.gov.justice.digital.hmpps.whereabouts.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "enabled_court")
@ApiModel(description = "Video Link Booking related information for a court")
class Court(
  @Id
  @ApiModelProperty(required = true, value = "The court identifier. Unique. Defined by courts registry.")
  val id: String,

  @ApiModelProperty(required = true, value = "A name for the court.")
  val name: String
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Court
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}
