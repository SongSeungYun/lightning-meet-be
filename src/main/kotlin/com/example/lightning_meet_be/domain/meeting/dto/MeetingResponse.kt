package com.example.lightning_meet_be.domain.meeting.dto

import java.time.LocalDateTime

data class MeetingResponse(
    val id: Long,
    val title: String,
    val content: String,
    val region: String,
    val location: String,
    val keywords: String?,
    val maxParticipants: Int,
    val currentParticipants: Int,
    val hostId: Long,
    val time: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
