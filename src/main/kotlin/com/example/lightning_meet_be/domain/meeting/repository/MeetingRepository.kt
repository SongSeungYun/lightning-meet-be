package com.example.lightning_meet_be.domain.meeting.repository

import com.example.lightning_meet_be.domain.meeting.entity.Meeting
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface MeetingRepository : JpaRepository<Meeting, Long> {
    fun findByRegionOrderByCreatedAtDesc(region: String, pageable: Pageable): Page<Meeting>
    fun findByRegionAndEventAtBetweenOrderByEventAtAsc(region: String, start: LocalDateTime, end: LocalDateTime): List<Meeting>
}
