package com.example.lightning_meet_be.global.exception

import com.example.lightning_meet_be.global.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse<Nothing>> {
        val error = e.errorCode
        return ResponseEntity
            .status(error.status)
            .body(ApiResponse(success = false, message = error.message))
    }

    // 예기치 못한 모든 예외 처리
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        e.printStackTrace()
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(ApiResponse(success = false, message = ErrorCode.INTERNAL_SERVER_ERROR.message))
    }
}
