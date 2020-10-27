package uk.gov.justice.digital.hmpps.whereabouts.model

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "ATTENDANCE_CHANGES")
@EntityListeners(AuditingEntityListener::class)
data class AttendanceChange(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  @OneToOne
  @JoinColumn(name = "attendance_id", referencedColumnName = "id")
  val attendance: Attendance,
  @Enumerated(EnumType.STRING)
  val changedFrom: AttendanceChangeValues,
  @Enumerated(EnumType.STRING)
  val changedTo: AttendanceChangeValues,
  @CreatedDate @Column(name = "CREATE_DATETIME", nullable = false)
  var createDateTime: LocalDateTime? = null,
  @CreatedBy
  @Column(name = "CREATE_USER_ID", nullable = false)
  var createUserId: String? = null
)
