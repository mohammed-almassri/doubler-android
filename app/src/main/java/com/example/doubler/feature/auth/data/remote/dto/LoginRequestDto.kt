package com.example.doubler.feature.auth.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String,
    val device_name: String = "Android App"
)