package com.example.lightning_meet_be.domain.auth.dto

data class MeResponse(
    val id: Long,
    val loginId: String,
    val nickname: String,
    val email: String,
    val role: String
)
