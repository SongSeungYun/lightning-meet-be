package com.example.lightning_meet_be.domain.meeting.entity

import com.example.lightning_meet_be.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "meetings")
class Meeting(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false, length = 50)
    var region: String,

    @Column(nullable = false)
    var maxParticipants: Int,

    @Column(nullable = false)
    var currentParticipants: Int = 1, // host 포함

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    val host: User,

    @Column(nullable = false)
    val eventAt: LocalDateTime,          // 모임 시간

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun touch() { updatedAt = LocalDateTime.now() }

    fun canJoin(): Boolean = currentParticipants < maxParticipants
    fun join() { currentParticipants += 1 }
    fun cancel() { currentParticipants = (currentParticipants - 1).coerceAtLeast(1) }
}
