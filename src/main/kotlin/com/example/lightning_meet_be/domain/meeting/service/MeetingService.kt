package com.example.lightning_meet_be.domain.meeting.service

import com.example.lightning_meet_be.domain.meeting.dto.*
import com.example.lightning_meet_be.domain.meeting.entity.Meeting
import com.example.lightning_meet_be.domain.meeting.entity.Participation
import com.example.lightning_meet_be.domain.meeting.repository.MeetingRepository
import com.example.lightning_meet_be.domain.meeting.repository.ParticipationRepository
import com.example.lightning_meet_be.domain.notification.entity.NotificationType
import com.example.lightning_meet_be.domain.notification.service.NotificationService
import com.example.lightning_meet_be.domain.user.repository.UserRepository
import com.example.lightning_meet_be.global.exception.CustomException
import com.example.lightning_meet_be.global.exception.ErrorCode
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.Duration

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val participationRepository: ParticipationRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    private fun toResponse(m: Meeting) = MeetingResponse(
        id = m.id!!,
        title = m.title,
        content = m.content,
        region = m.region,
        location = m.location,
        keywords = m.keywords,
        maxParticipants = m.maxParticipants,
        currentParticipants = m.currentParticipants,
        hostId = m.host.id!!,
        time = m.eventAt, // The entity still uses eventAt, but DTO uses time
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
            location = req.location,
            keywords = req.keywords,
            maxParticipants = req.maxParticipants,
            host = host,
            eventAt = req.time // Use time from DTO
        )
        val saved = meetingRepository.save(meeting)
        participationRepository.save(Participation(user = host, meeting = saved))
        return toResponse(saved)
    }

    fun get(id: Long): MeetingResponse =
        toResponse(meetingRepository.findById(id).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) })

    fun listAll(page: Int, size: Int): Page<MeetingResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return meetingRepository.findAll(pageable).map(::toResponse)
    }

    fun listByRegion(region: String, page: Int, size: Int): Page<MeetingResponse> {
        val pageable = PageRequest.of(page, size)
        return meetingRepository.findByRegionOrderByCreatedAtDesc(region, pageable).map(::toResponse)
    }

    @Cacheable(value = ["imminentMeetings"], key = "#userId", unless = "#result.empty", cacheManager = "cacheManager")
    fun listImminent(userId: Long): List<MeetingResponse> {
        val user = userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val userRegion = user.region ?: throw CustomException(ErrorCode.USER_REGION_NOT_SET) // User must have a region

        val now = LocalDateTime.now()
        val threeHoursLater = now.plus(Duration.ofHours(3))

        return meetingRepository.findByRegionAndEventAtBetweenOrderByEventAtAsc(userRegion, now, threeHoursLater)
            .filter { it.currentParticipants < it.maxParticipants } // Filter out full meetings
            .map(::toResponse)
    }

    fun listParticipating(userId: Long): List<MeetingResponse> {
        val user = userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        return participationRepository.findAllByUser(user)
            .map { it.meeting }
            .sortedByDescending { it.createdAt }
            .map(::toResponse)
    }

    @Transactional
    fun update(hostId: Long, id: Long, req: MeetingUpdateRequest): MeetingResponse {
        val meeting = meetingRepository.findById(id).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) }
        if (meeting.host.id != hostId) throw CustomException(ErrorCode.UNAUTHORIZED_USER)

        req.title?.let { meeting.title = it }
        req.content?.let { meeting.content = it }
        req.region?.let { meeting.region = it }
        req.location?.let { meeting.location = it }
        req.keywords?.let { meeting.keywords = it }
        req.maxParticipants?.let { meeting.maxParticipants = it }
        // req.time is not handled yet, need to decide if eventAt should be mutable

        meeting.touch()
        return toResponse(meeting)
    }

    @Transactional
    fun delete(hostId: Long, id: Long) {
        val meeting = meetingRepository.findById(id).orElseThrow { CustomException(ErrorCode.MEETING_NOT_FOUND) }
        if (meeting.host.id != hostId) throw CustomException(ErrorCode.UNAUTHORIZED_USER)
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

        notificationService.createNotification(
            user = meeting.host,
            type = NotificationType.JOIN_REQUEST,
            message = "'${user.nickname}'님이 회원님의 모임 '${meeting.title}'에 참여했습니다."
        )

        notificationService.createNotification(
            user = meeting.host,
            type = NotificationType.JOIN_REQUEST,
            message = "'${user.nickname}'님이 회원님의 모임 '${meeting.title}'에 참여했습니다."
        )
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
