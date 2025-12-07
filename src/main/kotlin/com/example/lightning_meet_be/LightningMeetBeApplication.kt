package com.example.lightning_meet_be

import com.example.lightning_meet_be.global.security.SecurityConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Import

@Import(SecurityConfig::class)

@SpringBootApplication(
	scanBasePackages = ["com.example.lightning_meet_be"]
)
@EnableCaching
class LightningMeetBeApplication

fun main(args: Array<String>) {
	runApplication<LightningMeetBeApplication>(*args)
}
