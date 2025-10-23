package com.example.doubler.feature.auth.data.remote.dto

data class LoginResponseDto(
    val message: String,
    val user: UserDto,
    val token: String
)