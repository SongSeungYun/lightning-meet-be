package com.example.lightning_meet_be.domain.user.dto

data class UserResponse(
    val id: Long,
    val loginId: String,
    val email: String,
    val nickname: String,
    val region: String?,
    val interests: String?
)
