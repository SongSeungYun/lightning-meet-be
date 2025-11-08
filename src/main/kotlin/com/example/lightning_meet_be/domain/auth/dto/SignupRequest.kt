package com.example.lightning_meet_be.domain.auth.dto

data class SignupRequest(
    val loginId: String,
    val password: String,
    val email: String,
    val nickname: String,
    val region: String?,
    val interests: String?
)
