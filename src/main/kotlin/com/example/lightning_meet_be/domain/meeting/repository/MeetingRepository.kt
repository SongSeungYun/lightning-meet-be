package com.example.lightning_meet_be.domain.meeting.repository

import com.example.lightning_meet_be.domain.meeting.entity.Meeting
import org.springframework.data.jpa.repository.JpaRepository

interface MeetingRepository : JpaRepository<Meeting, Long> {
    fun findAllByRegionOrderByCreatedAtDesc(region: String): List<Meeting>
}
