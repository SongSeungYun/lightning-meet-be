package com.example.lightning_meet_be.domain.meeting.controller

import com.example.lightning_meet_be.domain.meeting.dto.MeetingCreateRequest
import com.example.lightning_meet_be.domain.meeting.dto.MeetingResponse
import com.example.lightning_meet_be.domain.meeting.service.MeetingService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [MeetingController::class],
    // 모든 @Component, @Service, @Repository 등 자동 스캔 비활성화
    useDefaultFilters = false,
    // MeetingController만 명시적으로 포함
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [MeetingController::class]
        )
    ]
)
class MeetingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @MockkBean
    private lateinit var meetingService: MeetingService

    private val meetingResponse = MeetingResponse(
        id = 1,
        title = "Test Meeting",
        content = "Test Content",
        region = "서울",
        location = "강남",
        keywords = "test, meeting",
        maxParticipants = 10,
        currentParticipants = 1,
        hostId = 1L,
        time = LocalDateTime.now(),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @Test
    @DisplayName("GET /api/meetings/{id} - 모임 단건 조회")
    @WithMockCustomUser
    fun `get meeting by id should return meeting response`() {
        val meetingId = 1L
        every { meetingService.get(meetingId) } returns meetingResponse

        mockMvc.perform(get("/api/meetings/{id}", meetingId).with(csrf()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    @DisplayName("GET /api/meetings/imminent - 마감 임박 모임 조회")
    @WithMockCustomUser(userId = "1")
    fun `get imminent meetings should return a list of meetings`() {
        every { meetingService.listImminent(1L) } returns listOf(meetingResponse)

        mockMvc.perform(get("/api/meetings/imminent").with(csrf()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    @DisplayName("GET /api/meetings - 전체 모임 목록 조회")
    @WithMockCustomUser
    fun `list all meetings should return a page of meetings`() {
        every { meetingService.listAll(0, 10) } returns PageImpl(listOf(meetingResponse))

        mockMvc.perform(get("/api/meetings").param("page", "0").param("size", "10").with(csrf()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    @DisplayName("POST /api/meetings - 모임 생성")
    @WithMockCustomUser(userId = "1")
    fun `create meeting should return created meeting response`() {
        val request = MeetingCreateRequest( "New", "Content", "서울", "홍대", "new", 5, LocalDateTime.now())
        every { meetingService.create(1L, any()) } returns meetingResponse

        mockMvc.perform(
            post("/api/meetings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}
