package com.example.lightning_meet_be.domain.meeting.service

import com.example.lightning_meet_be.domain.meeting.dto.MeetingResponse
import com.example.lightning_meet_be.domain.meeting.entity.Meeting
import com.example.lightning_meet_be.domain.meeting.repository.MeetingRepository
import com.example.lightning_meet_be.domain.meeting.repository.ParticipationRepository
import com.example.lightning_meet_be.domain.notification.service.NotificationService
import com.example.lightning_meet_be.domain.user.entity.User
import com.example.lightning_meet_be.domain.user.repository.UserRepository
import com.example.lightning_meet_be.global.exception.CustomException
import com.example.lightning_meet_be.global.exception.ErrorCode
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class MeetingServiceTest {

    @MockK
    lateinit var meetingRepository: MeetingRepository

    @MockK
    lateinit var participationRepository: ParticipationRepository

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var notificationService: NotificationService

    @InjectMockKs
    lateinit var meetingService: MeetingService

    @BeforeEach
    fun setUp() {
        // Clear mocks before each test
        clearAllMocks()
    }

    private fun mockUser(id: Long = 1L, region: String? = "서울") = User(
        id = id,
        loginId = "testUser",
        password = "password",
        email = "test@example.com",
        nickname = "TestUser",
        region = region
    )

    private fun mockMeeting(
        id: Long = 1L,
        title: String = "Test Meeting",
        region: String = "서울",
        eventAt: LocalDateTime = LocalDateTime.now().plusHours(1),
        createdAt: LocalDateTime = LocalDateTime.now(),
        currentParticipants: Int = 1,
        maxParticipants: Int = 5,
        host: User = mockUser()
    ) = Meeting(
        id = id,
        title = title,
        content = "Content",
        region = region,
        location = "Location",
        keywords = "Keywords",
        maxParticipants = maxParticipants,
        currentParticipants = currentParticipants,
        host = host,
        eventAt = eventAt,
        createdAt = createdAt
    )

    @Test
    @DisplayName("listImminent - 3시간 이내, 사용자 지역에 맞는, 만석이 아닌 모임 목록을 반환해야 한다.")
    fun `listImminent should return non-full meetings within 3 hours matching user region`() {
        // Given
        val userId = 1L
        val userRegion = "서울"
        val mockUser = mockUser(id = userId, region = userRegion)

        val imminentMeeting1 = mockMeeting(
            id = 101L,
            region = userRegion,
            eventAt = LocalDateTime.now().plusMinutes(30),
            currentParticipants = 2,
            maxParticipants = 5
        )
        val imminentMeeting2 = mockMeeting(
            id = 102L,
            region = userRegion,
            eventAt = LocalDateTime.now().plusHours(2),
            currentParticipants = 1,
            maxParticipants = 3
        )
        val fullMeeting = mockMeeting(
            id = 103L,
            region = userRegion,
            eventAt = LocalDateTime.now().plusHours(1),
            currentParticipants = 5,
            maxParticipants = 5 // Full
        )
        val tooLateMeeting = mockMeeting(
            id = 104L,
            region = userRegion,
            eventAt = LocalDateTime.now().plusHours(4),
            currentParticipants = 1,
            maxParticipants = 5 // Out of 3-hour window
        )
        val otherRegionMeeting = mockMeeting(
            id = 105L,
            region = "부산", // Other region
            eventAt = LocalDateTime.now().plusHours(1),
            currentParticipants = 1,
            maxParticipants = 5
        )

        // Mock the repository call to return a filtered list based on the arguments
        // This is necessary because the `any()` matchers are loose and we need to simulate
        // the repository's filtering behavior accurately within the test.
        every { userRepository.findById(userId) } returns Optional.of(mockUser)
        every { meetingRepository.findByRegionAndEventAtBetweenOrderByEventAtAsc(any(), any(), any()) } answers {
            val regionArg = it.invocation.args[0] as String
            val startArg = it.invocation.args[1] as LocalDateTime
            val endArg = it.invocation.args[2] as LocalDateTime
            
            // Simulate the repository's filtering based on the arguments
            listOf(imminentMeeting1, imminentMeeting2, fullMeeting, tooLateMeeting, otherRegionMeeting).filter { m ->
                m.region == regionArg && 
                m.eventAt.isAfter(startArg.minusSeconds(1)) && // Add a small buffer for precision
                m.eventAt.isBefore(endArg.plusSeconds(1))      // Add a small buffer for precision
            }
        }

        // When
        val result = meetingService.listImminent(userId)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == imminentMeeting1.id })
        assertTrue(result.any { it.id == imminentMeeting2.id })
        assertFalse(result.any { it.id == fullMeeting.id })
        assertFalse(result.any { it.id == tooLateMeeting.id })
        assertFalse(result.any { it.id == otherRegionMeeting.id })

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { meetingRepository.findByRegionAndEventAtBetweenOrderByEventAtAsc(any(), any(), any()) }
    }

    @Test
    @DisplayName("listImminent - 사용자를 찾을 수 없는 경우 CustomException.USER_NOT_FOUND를 던져야 한다.")
    fun `listImminent should throw CustomException USER_NOT_FOUND when user not found`() {
        // Given
        val userId = 1L
        every { userRepository.findById(userId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows(CustomException::class.java) {
            meetingService.listImminent(userId)
        }
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { meetingRepository.findByRegionAndEventAtBetweenOrderByEventAtAsc(any(), any(), any()) }
    }

    @Test
    @DisplayName("listImminent - 사용자 지역이 설정되지 않은 경우 CustomException.USER_REGION_NOT_SET를 던져야 한다.")
    fun `listImminent should throw CustomException USER_REGION_NOT_SET when user region is not set`() {
        // Given
        val userId = 1L
        val mockUserWithoutRegion = mockUser(id = userId, region = null) // Explicitly set region to null
        every { userRepository.findById(userId) } returns Optional.of(mockUserWithoutRegion)

        // When & Then
        val exception = assertThrows(CustomException::class.java) {
            meetingService.listImminent(userId)
        }
        assertEquals(ErrorCode.USER_REGION_NOT_SET, exception.errorCode)

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { meetingRepository.findByRegionAndEventAtBetweenOrderByEventAtAsc(any(), any(), any()) }
    }

    @Test
    @DisplayName("listAll - 페이지네이션과 정렬이 적용된 모든 모임을 반환해야 한다.")
    fun `listAll should return all meetings with pagination and sorting`() {
        // Given
        val page = 0
        val size = 10
        val mockMeetings = listOf(
            mockMeeting(id = 1L, eventAt = LocalDateTime.now().minusDays(1), createdAt = LocalDateTime.now().minusDays(1)),
            mockMeeting(id = 2L, eventAt = LocalDateTime.now().minusDays(2), createdAt = LocalDateTime.now().minusDays(2))
        )
        val mockPage = PageImpl(mockMeetings, PageRequest.of(page, size, Sort.Direction.DESC, "createdAt"), 2)

        every { meetingRepository.findAll(any<PageRequest>()) } returns mockPage

        // When
        val result = meetingService.listAll(page, size)

        // Then
        assertEquals(2, result.content.size)
        assertEquals(1L, result.content[0].id) // Ensure sorting is handled by mock if any
        assertEquals(2L, result.content[1].id)

        verify(exactly = 1) { meetingRepository.findAll(any<PageRequest>()) }
    }

    @Test
    @DisplayName("listByRegion - 페이지네이션과 지역 필터가 적용된 모임을 반환해야 한다.")
    fun `listByRegion should return meetings filtered by region with pagination`() {
        // Given
        val region = "부산"
        val page = 0
        val size = 10
        val mockMeetings = listOf(
            mockMeeting(id = 3L, region = region, createdAt = LocalDateTime.now()),
            mockMeeting(id = 4L, region = region, createdAt = LocalDateTime.now().minusHours(1))
        )
        val mockPage = PageImpl(mockMeetings, PageRequest.of(page, size), 2)

        every { meetingRepository.findByRegionOrderByCreatedAtDesc(eq(region), any<PageRequest>()) } returns mockPage

        // When
        val result = meetingService.listByRegion(region, page, size)

        // Then
        assertEquals(2, result.content.size)
        assertTrue(result.content.all { it.region == region })

        verify(exactly = 1) { meetingRepository.findByRegionOrderByCreatedAtDesc(eq(region), any<PageRequest>()) }
    }
}