package uk.gov.justice.digital.hmpps.whereabouts.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "enabled_court")
@ApiModel(description = "Video Link Booking related information for a court")
data class Court(
  @Id
  @ApiModelProperty(required = true, value = "The court identifier. Unique. Defined by courts registry.")
  val id: String,

  @ApiModelProperty(required = true, value = "A name for the court.")
  val name: String
)
