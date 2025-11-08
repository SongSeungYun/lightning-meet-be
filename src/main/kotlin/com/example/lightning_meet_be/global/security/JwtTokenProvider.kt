package com.example.lightning_meet_be.global.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-exp}") private val accessExpMs: Long,
    @Value("\${jwt.refresh-exp}") private val refreshExpMs: Long
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8)) }

    fun createAccessToken(userId: Long, loginId: String, role: String): String {
        val now = Date()
        val exp = Date(now.time + accessExpMs)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("lid", loginId)
            .claim("role", role)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(userId: Long, loginId: String, role: String): String {
        val now = Date()
        val exp = Date(now.time + refreshExpMs)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("lid", loginId)
            .claim("role", role)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }

    fun parse(token: String): JwtUserPrincipal {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        val userId = claims.subject.toLong()
        val loginId = claims["lid"] as String
        val role = claims["role"] as String
        return JwtUserPrincipal(userId, loginId, role)
    }
}
