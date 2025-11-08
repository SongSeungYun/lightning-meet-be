package com.example.lightning_meet_be.domain.auth.service

import com.example.lightning_meet_be.domain.auth.dto.*
import com.example.lightning_meet_be.domain.auth.entity.RefreshToken
import com.example.lightning_meet_be.domain.auth.repository.RefreshTokenRepository
import com.example.lightning_meet_be.domain.user.entity.User
import com.example.lightning_meet_be.domain.user.repository.UserRepository
import com.example.lightning_meet_be.global.exception.CustomException
import com.example.lightning_meet_be.global.exception.ErrorCode
import com.example.lightning_meet_be.global.security.JwtTokenProvider
import com.example.lightning_meet_be.global.security.JwtUserPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider
) {

    /**
     * 회원가입
     */
    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.existsByLoginId(request.loginId))
            throw CustomException(ErrorCode.DUPLICATE_LOGIN_ID)
        if (userRepository.existsByEmail(request.email))
            throw CustomException(ErrorCode.DUPLICATE_EMAIL)

        val user = User(
            loginId = request.loginId,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            nickname = request.nickname,
            region = request.region,
            interests = request.interests
        )

        val saved = userRepository.save(user)
        return AuthResponse(
            id = saved.id!!,
            nickname = saved.nickname,
            email = saved.email
        )
    }

    /**
     * 로그인 (Access / Refresh Token 발급)
     */
    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByLoginId(request.loginId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        if (!passwordEncoder.matches(request.password, user.password))
            throw CustomException(ErrorCode.INVALID_CREDENTIALS)

        val access = tokenProvider.createAccessToken(user.id!!, user.loginId, user.role.name)
        val refresh = tokenProvider.createRefreshToken(user.id!!, user.loginId, user.role.name)

        // Refresh Token upsert
        val rt = refreshTokenRepository.findByUser(user)
            .orElse(RefreshToken(user = user, token = refresh, expiryDate = LocalDateTime.now().plusDays(14)))

        rt.token = refresh
        rt.expiryDate = LocalDateTime.now().plusDays(14)
        refreshTokenRepository.save(rt)

        return TokenResponse(access, refresh)
    }

    /**
     * Refresh Token으로 새 토큰 재발급
     */
    @Transactional
    fun refresh(req: RefreshRequest): TokenResponse {
        val stored = refreshTokenRepository.findByToken(req.refreshToken)
            .orElseThrow { CustomException(ErrorCode.INVALID_TOKEN) }

        val principal = try {
            tokenProvider.parse(req.refreshToken)
        } catch (_: Exception) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }

        // 만료일 체크
        if (stored.expiryDate != null && stored.expiryDate!!.isBefore(LocalDateTime.now()))
            throw CustomException(ErrorCode.EXPIRED_TOKEN)

        val user = stored.user
        val newAccess = tokenProvider.createAccessToken(user.id!!, user.loginId, user.role.name)
        val newRefresh = tokenProvider.createRefreshToken(user.id!!, user.loginId, user.role.name)

        stored.token = newRefresh
        stored.expiryDate = LocalDateTime.now().plusDays(14)
        refreshTokenRepository.save(stored)

        return TokenResponse(newAccess, newRefresh)
    }

    /**
     * 인증된 사용자 정보 반환
     */
    fun me(principal: JwtUserPrincipal): MeResponse {
        val user = userRepository.findById(principal.userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        return MeResponse(
            id = user.id!!,
            loginId = user.loginId,
            nickname = user.nickname,
            email = user.email,
            role = user.role.name
        )
    }
}
