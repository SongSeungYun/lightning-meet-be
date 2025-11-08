package com.example.lightning_meet_be.domain.auth.repository

import com.example.lightning_meet_be.domain.auth.entity.RefreshToken
import com.example.lightning_meet_be.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByUser(user: User): Optional<RefreshToken>
    fun findByToken(token: String): Optional<RefreshToken>
    fun deleteByUser(user: User)
}
