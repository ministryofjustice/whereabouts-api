package uk.gov.justice.digital.hmpps.whereabouts.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.Hibernate

@Entity
@Table(name = "enabled_court")
@ApiModel(description = "Video Link Booking related information for a court")
class Court(
  @Id
  @ApiModelProperty(
    required = true,
    value = "The court identifier. Unique. Defined by courts registry or self generated.",
  )
  val id: String,
  /*
  The pattern how to generate court ID
  We have a bit of a convention of having the suffix based on the type of court:
  MC -> Magistrates court
  CC -> crown courts
  CT -> county courts
  justice centres are contain multiple types but we've gone with a convention of MC
  Each location tends to have its own prefix: Workington -> WRK
  So Workington Crown Court would be WRKCC
*/

  @ApiModelProperty(value = "A name for the court.")
  val name: String,

  @ApiModelProperty(value = "Court email address.")
  val email: String? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Court
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}
