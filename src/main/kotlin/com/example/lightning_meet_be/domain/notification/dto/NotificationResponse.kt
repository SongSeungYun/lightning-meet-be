package com.example.lightning_meet_be.domain.notification.dto

import com.example.lightning_meet_be.domain.notification.entity.Notification
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val type: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = notification.id!!,
            type = notification.type.name,
            message = notification.message,
            isRead = notification.isRead,
            createdAt = notification.createdAt
        )
    }
}
