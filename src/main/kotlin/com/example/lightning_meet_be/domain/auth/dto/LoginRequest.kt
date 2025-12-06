package com.example.lightning_meet_be.domain.auth.dto

data class LoginRequest(
    val loginId: String,
    val password: String
)
