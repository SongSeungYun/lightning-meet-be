package com.example.lightning_meet_be.domain.meeting.controller

import com.example.lightning_meet_be.global.security.JwtUserPrincipal
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithMockCustomUser(
    val userId: String = "1",
    val role: String = "USER"
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
    override fun createSecurityContext(annotation: WithMockCustomUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${annotation.role}"))
        val principal = JwtUserPrincipal(
            userId = annotation.userId.toLong(),
            loginId = "testuser", // Provide a dummy loginId for the test context
            role = annotation.role
        )
        val auth = UsernamePasswordAuthenticationToken(principal, "password", authorities)
        context.authentication = auth
        return context
    }
}
