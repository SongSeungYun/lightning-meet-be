package com.example.lightning_meet_be.domain.notification.service

import com.example.lightning_meet_be.domain.notification.dto.NotificationResponse
import com.example.lightning_meet_be.domain.notification.entity.Notification
import com.example.lightning_meet_be.domain.notification.entity.NotificationType
import com.example.lightning_meet_be.domain.notification.repository.NotificationRepository
import com.example.lightning_meet_be.domain.user.entity.User
import com.example.lightning_meet_be.global.exception.CustomException
import com.example.lightning_meet_be.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    fun createNotification(user: User, type: NotificationType, message: String) {
        val notification = Notification(
            user = user,
            type = type,
            message = message
        )
        notificationRepository.save(notification)
    }

    @Transactional(readOnly = true)
    fun getNotificationsForUser(userId: Long): List<NotificationResponse> {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .map { NotificationResponse.from(it) }
    }

    @Transactional
    fun markAsRead(notificationId: Long, userId: Long): NotificationResponse {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { CustomException(ErrorCode.NOTIFICATION_NOT_FOUND) }

        if (notification.user.id != userId) {
            throw CustomException(ErrorCode.UNAUTHORIZED_USER)
        }

        notification.read()
        return NotificationResponse.from(notification)
    }

    @Transactional
    fun markAllAsRead(userId: Long) {
        val notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        notifications.forEach { it.read() }
    }
}
