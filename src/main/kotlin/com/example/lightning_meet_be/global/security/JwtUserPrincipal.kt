package com.example.lightning_meet_be.global.security

data class JwtUserPrincipal(
    val userId: Long,
    val loginId: String,
    val role: String
)
