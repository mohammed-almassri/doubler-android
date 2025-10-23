package com.example.doubler.feature.auth.domain.repository

import com.example.doubler.feature.auth.data.remote.dto.RegisterRequestDto
import com.example.doubler.feature.auth.data.remote.dto.RegisterResponseDto
import com.example.doubler.feature.auth.domain.model.User

interface AuthRepository{
    suspend fun register(
        name: String,
        email: String,
        password: String,
        deviceName: String,
    ): User
    
    suspend fun login(
        email: String,
        password: String,
        deviceName: String,
    ): User
}