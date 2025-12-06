package com.example.lightning_meet_be.domain.meeting.dto

import java.time.LocalDateTime

data class MeetingUpdateRequest(
    val title: String?,
    val content: String?,
    val region: String?,
    val location: String?,
    val keywords: String?,
    val maxParticipants: Int?,
    val time: LocalDateTime?
)