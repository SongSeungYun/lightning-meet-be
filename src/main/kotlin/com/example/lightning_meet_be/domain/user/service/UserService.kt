package com.example.lightning_meet_be.domain.user.service

import com.example.lightning_meet_be.domain.user.dto.*
import com.example.lightning_meet_be.domain.user.entity.User
import com.example.lightning_meet_be.domain.user.repository.UserRepository
import com.example.lightning_meet_be.global.exception.CustomException
import com.example.lightning_meet_be.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getUser(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        return user.toResponse()
    }

    fun getAllUsers(): List<UserResponse> =
        userRepository.findAll().map { it.toResponse() }

    @Transactional
    fun createUser(request: UserCreateRequest): UserResponse {
        if (userRepository.existsByLoginId(request.loginId))
            throw CustomException(ErrorCode.DUPLICATE_LOGIN_ID)
        if (userRepository.existsByEmail(request.email))
            throw CustomException(ErrorCode.DUPLICATE_EMAIL)

        val user = User(
            loginId = request.loginId,
            password = request.password,
            email = request.email,
            nickname = request.nickname,
            region = request.region,
            interests = request.interests
        )

        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        request.nickname?.let { user.nickname = it }
        request.region?.let { user.region = it }
        request.interests?.let { user.interests = it }

        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id))
            throw CustomException(ErrorCode.USER_NOT_FOUND)
        userRepository.deleteById(id)
    }

    private fun User.toResponse() = UserResponse(
        id = this.id!!,
        loginId = this.loginId,
        email = this.email,
        nickname = this.nickname,
        region = this.region,
        interests = this.interests
    )
}
