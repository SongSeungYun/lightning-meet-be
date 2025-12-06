package com.example.lightning_meet_be.domain.meeting.controller

import com.example.lightning_meet_be.domain.meeting.dto.MeetingCreateRequest
import com.example.lightning_meet_be.domain.meeting.dto.MeetingUpdateRequest
import com.example.lightning_meet_be.domain.meeting.service.MeetingService
import com.example.lightning_meet_be.global.response.ResponseUtils
import com.example.lightning_meet_be.global.security.JwtUserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/meetings")
class MeetingController(
    private val meetingService: MeetingService
) {
    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: JwtUserPrincipal,
        @RequestBody req: MeetingCreateRequest
    ) = ResponseUtils.success(data = meetingService.create(principal.userId, req))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) =
        ResponseUtils.success(data = meetingService.get(id))

    @GetMapping
    fun list(
        @RequestParam(required = false) region: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ) =
        ResponseUtils.success(
            data = if (region.isNullOrBlank()) meetingService.listAll(page, size)
            else meetingService.listByRegion(region, page, size)
        )

    @GetMapping("/my-participating")
    fun listMyParticipatingMeetings(
        @AuthenticationPrincipal principal: JwtUserPrincipal
    ) = ResponseUtils.success(data = meetingService.listParticipating(principal.userId))

    @GetMapping("/imminent")
    fun listImminentMeetings(
        @AuthenticationPrincipal principal: JwtUserPrincipal
    ) = ResponseUtils.success(data = meetingService.listImminent(principal.userId))

    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal principal: JwtUserPrincipal,
        @PathVariable id: Long,
        @RequestBody req: MeetingUpdateRequest
    ) = ResponseUtils.success(data = meetingService.update(principal.userId, id, req))

    @DeleteMapping("/{id}")
    fun delete(
        @AuthenticationPrincipal principal: JwtUserPrincipal,
        @PathVariable id: Long
    ) = ResponseUtils.success(message = "deleted").also {
        meetingService.delete(principal.userId, id)
    }

    @PostMapping("/{id}/join")
    fun join(
        @AuthenticationPrincipal principal: JwtUserPrincipal,
        @PathVariable id: Long
    ) = ResponseUtils.success(message = "joined").also {
        meetingService.join(principal.userId, id)
    }

    @PostMapping("/{id}/leave")
    fun leave(
        @AuthenticationPrincipal principal: JwtUserPrincipal,
        @PathVariable id: Long
    ) = ResponseUtils.success(message = "left").also {
        meetingService.cancelJoin(principal.userId, id)
    }
}
