package com.example.lightning_meet_be.domain.user.repository

import com.example.lightning_meet_be.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByLoginId(loginId: String): Optional<User>
    fun existsByLoginId(loginId: String): Boolean
    fun existsByEmail(email: String): Boolean
}
