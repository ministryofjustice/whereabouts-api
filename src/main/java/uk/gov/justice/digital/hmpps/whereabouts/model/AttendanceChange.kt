package uk.gov.justice.digital.hmpps.whereabouts.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

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
  @CreatedDate
  @Column(name = "CREATE_DATETIME", nullable = false)
  var createDateTime: LocalDateTime? = null,
  @CreatedBy
  @Column(name = "CREATE_USER_ID", nullable = false)
  var createUserId: String? = null,
)
