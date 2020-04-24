package uk.gov.justice.digital.hmpps.whereabouts.model

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*



@Entity
@Table(name = "ATTENDANCE_CHANGES")
@EntityListeners(AuditingEntityListener::class)
data class AttendanceChange(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val attendanceId: Long,
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
