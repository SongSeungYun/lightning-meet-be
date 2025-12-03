package com.example.lightning_meet_be.domain.notification.entity

import com.example.lightning_meet_be.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false)
    val message: String,

    @Column(name = "is_read")
    var isRead: Boolean = false,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun read() {
        isRead = true
    }
}

enum class NotificationType {
    NEW_MEETING,
    JOIN_REQUEST,
    APPROVED,
    CANCELLED
}