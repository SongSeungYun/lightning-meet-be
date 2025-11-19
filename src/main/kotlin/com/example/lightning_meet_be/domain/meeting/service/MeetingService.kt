package com.example.lightning_meet_be.domain.meeting.service

import com.example.lightning_meet_be.domain.meeting.dto.*
import com.example.lightning_meet_be.domain.meeting.entity.Meeting
import com.example.lightning_meet_be.domain.meeting.entity.Participation
import com.example.lightning_meet_be.domain.meeting.repository.MeetingRepository
import com.example.lightning_meet_be.domain.meeting.repository.ParticipationRepository
import com.example.lightning_meet_be.domain.user.repository.UserRepository
import com.example.lightning_meet_be.global.exception.CustomException
import com.example.lightning_meet_be.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val participationRepository: ParticipationRepository,
    private val userRepository: UserRepository
) {
    private fun toResponse(m: Meeting) = MeetingResponse(
        id = m.id!!,
        title = m.title,
        content = m.content,
        region = m.region,
        maxParticipants = m.maxParticipants,
        currentParticipants = m.currentParticipants,
        hostId = m.host.id!!,
        eventAt = m.eventAt,
        createdAt = m.createdAt,
        updatedAt = m.updatedAt
    )

    @Transactional
    fun create(hostId: Long, req: MeetingCreateRequest): MeetingResponse {
        val host = userRepository.findById(hostId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val meeting = Meeting(
            title = req.title,
            content = req.content,
            region = req.region,
            maxParticipants = req.maxParticipants,
            host = host,
            eventAt = req.eventAt
        )
        val saved = meetingRepository.save(meeting)
        // 생성시 호스트 자동 참여(선택): Participation을 만들고 currentParticipants는 1로 유지
        participationRepository.save(Participation(user = host, meeting = saved))
        return toResponse(saved)
    }

    fun get(id: Long): MeetingResponse =
        toResponse(meetingRepository.findById(id).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) })

    fun listAll(): List<MeetingResponse> =
        meetingRepository.findAll().sortedByDescending { it.createdAt }.map(::toResponse)

    fun listByRegion(region: String): List<MeetingResponse> =
        meetingRepository.findAllByRegionOrderByCreatedAtDesc(region).map(::toResponse)

    @Transactional
    fun update(hostId: Long, id: Long, req: MeetingUpdateRequest): MeetingResponse {
        val meeting = meetingRepository.findById(id).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) }
        if (meeting.host.id != hostId) throw CustomException(ErrorCode.UNAUTHORIZED_USER)

        meeting.title = req.title
        meeting.content = req.content
        meeting.region = req.region
        meeting.maxParticipants = req.maxParticipants
        meeting.touch()
        return toResponse(meeting)
    }

    @Transactional
    fun delete(hostId: Long, id: Long) {
        val meeting = meetingRepository.findById(id).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) }
        if (meeting.host.id != hostId) throw CustomException(ErrorCode.UNAUTHORIZED_USER)
        // 참여 기록 먼저 제거
        participationRepository.findAll().filter { it.meeting.id == id }.forEach { participationRepository.delete(it) }
        meetingRepository.delete(meeting)
    }

    @Transactional
    fun join(userId: Long, meetingId: Long) {
        val user = userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val meeting = meetingRepository.findById(meetingId).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) }

        if (!meeting.canJoin()) throw CustomException(ErrorCode.MEETING_FULL)
        if (participationRepository.findByUserAndMeeting(user, meeting) != null)
            throw CustomException(ErrorCode.DUPLICATE_PARTICIPATION)

        participationRepository.save(Participation(user = user, meeting = meeting))
        meeting.join()
        meeting.touch()
    }

    @Transactional
    fun cancelJoin(userId: Long, meetingId: Long) {
        val user = userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val meeting = meetingRepository.findById(meetingId).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) }

        val p = participationRepository.findByUserAndMeeting(user, meeting)
            ?: throw CustomException(ErrorCode.INVALID_REQUEST)

        participationRepository.delete(p)
        meeting.cancel()
        meeting.touch()
    }
}
