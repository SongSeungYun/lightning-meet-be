package com.example.lightning_meet_be.global.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun now(): LocalDateTime = LocalDateTime.now()

    fun format(date: LocalDateTime): String = date.format(formatter)

    fun minutesUntil(target: LocalDateTime): Long =
        Duration.between(LocalDateTime.now(), target).toMinutes()
}