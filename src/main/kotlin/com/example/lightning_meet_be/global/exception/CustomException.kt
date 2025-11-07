package com.example.lightning_meet_be.global.exception

class CustomException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)
