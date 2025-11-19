package com.example.lightning_meet_be.domain.meeting.repository

import com.example.lightning_meet_be.domain.meeting.entity.Meeting
import com.example.lightning_meet_be.domain.meeting.entity.Participation
import com.example.lightning_meet_be.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipationRepository : JpaRepository<Participation, Long> {
    fun findByUserAndMeeting(user: User, meeting: Meeting): Participation?
    fun countByMeeting(meeting: Meeting): Int
    fun findAllByUser(user: User): List<Participation>
}
