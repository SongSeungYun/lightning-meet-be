package com.example.lightning_meet_be.domain.notification.controller

import com.example.lightning_meet_be.domain.notification.service.NotificationService
import com.example.lightning_meet_be.global.response.ResponseUtils
import com.example.lightning_meet_be.global.security.JwtUserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal principal: JwtUserPrincipal
    ) = ResponseUtils.success(data = notificationService.getNotificationsForUser(principal.userId))

    @PostMapping("/{id}/read")
    fun markAsRead(
        @AuthenticationPrincipal principal: JwtUserPrincipal,
        @PathVariable id: Long
    ) = ResponseUtils.success(data = notificationService.markAsRead(id, principal.userId))

    @PostMapping("/read-all")
    fun markAllAsRead(
        @AuthenticationPrincipal principal: JwtUserPrincipal
    ) = ResponseUtils.success(message = "All marked as read").also {
        notificationService.markAllAsRead(principal.userId)
    }
}
