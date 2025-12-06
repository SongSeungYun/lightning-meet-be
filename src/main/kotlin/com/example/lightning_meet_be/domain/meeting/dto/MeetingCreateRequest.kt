package com.example.lightning_meet_be.domain.meeting.dto

import java.time.LocalDateTime

data class MeetingCreateRequest(
    val title: String,
    val content: String,
    val region: String,
    val location: String,
    val keywords: String? = null,
    val maxParticipants: Int,
    val time: LocalDateTime
)