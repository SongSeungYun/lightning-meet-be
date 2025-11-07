package com.example.lightning_meet_be.global.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.slf4j.LoggerFactory

@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI
        val method = request.method
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val start = System.currentTimeMillis()

        log.info("[${method}] ${uri}${queryString} 요청 시작")

        filterChain.doFilter(request, response)

        val duration = System.currentTimeMillis() - start
        val status = response.status

        log.info("[${method}] ${uri}${queryString} 요청 종료 (status=${status}, ${duration}ms)")
    }
}