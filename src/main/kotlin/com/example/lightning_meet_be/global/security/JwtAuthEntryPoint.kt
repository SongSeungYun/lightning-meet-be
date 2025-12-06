package com.example.lightning_meet_be.global.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthEntryPoint : AuthenticationEntryPoint {
    private val mapper = jacksonObjectMapper()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = 401
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val body = mapOf(
            "success" to false,
            "message" to "UNAUTHORIZED"
        )
        response.writer.write(mapper.writeValueAsString(body))
    }
}
