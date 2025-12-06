package com.example.lightning_meet_be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class LightningMeetBeApplication

fun main(args: Array<String>) {
	runApplication<LightningMeetBeApplication>(*args)
}
