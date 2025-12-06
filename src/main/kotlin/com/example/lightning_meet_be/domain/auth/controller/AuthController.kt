package com.example.lightning_meet_be.domain.auth.controller

import com.example.lightning_meet_be.domain.auth.dto.*
import com.example.lightning_meet_be.domain.auth.service.AuthService
import com.example.lightning_meet_be.global.response.ResponseUtils
import com.example.lightning_meet_be.global.security.JwtUserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest) =
        ResponseUtils.success(data = authService.signup(request))

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest) =
        ResponseUtils.success(data = authService.login(request))

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest) =
        ResponseUtils.success(data = authService.refresh(request))

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: JwtUserPrincipal) =
        ResponseUtils.success(data = authService.me(principal))
}
