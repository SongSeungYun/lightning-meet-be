package com.example.lightning_meet_be.domain.meeting.entity

import com.example.lightning_meet_be.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "participations",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "meeting_id"])]
)
class Participation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "meeting_id", nullable = false)
    val meeting: Meeting,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
