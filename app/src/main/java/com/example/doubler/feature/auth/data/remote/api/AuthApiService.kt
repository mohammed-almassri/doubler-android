package com.example.doubler.feature.auth.data.remote.api

import com.example.doubler.feature.auth.data.remote.dto.LoginRequestDto
import com.example.doubler.feature.auth.data.remote.dto.LoginResponseDto
import com.example.doubler.feature.auth.data.remote.dto.RegisterRequestDto
import com.example.doubler.feature.auth.data.remote.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body requestDto: RegisterRequestDto): RegisterResponseDto
    
    @POST("auth/login")
    suspend fun login(@Body requestDto: LoginRequestDto): LoginResponseDto
}