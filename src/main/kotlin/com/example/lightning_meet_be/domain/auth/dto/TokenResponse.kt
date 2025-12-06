package com.example.lightning_meet_be.domain.auth.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
