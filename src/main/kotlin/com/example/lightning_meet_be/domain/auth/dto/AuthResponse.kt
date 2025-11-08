package com.example.lightning_meet_be.domain.auth.dto

data class AuthResponse(
    val id: Long,
    val nickname: String,
    val email: String
)
