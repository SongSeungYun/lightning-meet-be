package com.example.lightning_meet_be.global.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * ✅ 공통 API 응답 유틸
 * 모든 Controller에서 일관된 형태로 응답을 반환하기 위함.
 */
object ResponseUtils {

    fun <T> success(
        message: String = "success",
        data: T? = null,
        status: HttpStatus = HttpStatus.OK
    ): ResponseEntity<ApiResponse<T>> =
        ResponseEntity.status(status).body(ApiResponse(success = true, message = message, data = data))

    fun error(
        message: String = "error",
        status: HttpStatus = HttpStatus.BAD_REQUEST
    ): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(status).body(ApiResponse(success = false, message = message))
}

/**
 * ✅ API 응답 데이터 클래스
 * 모든 응답은 동일한 구조를 따름
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)