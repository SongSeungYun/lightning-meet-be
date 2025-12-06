package com.example.lightning_meet_be.domain.user.dto

data class UserCreateRequest(
    val loginId: String,
    val password: String,
    val email: String,
    val nickname: String,
    val region: String?,
    val interests: String?
)

data class UserUpdateRequest(
    val nickname: String?,
    val region: String?,
    val interests: String?
)
