package com.example.doubler.feature.auth.data.repository

import com.example.doubler.feature.auth.data.local.PreferencesDataSource
import com.example.doubler.feature.auth.data.remote.api.AuthApiService
import com.example.doubler.feature.auth.data.remote.dto.LoginRequestDto
import com.example.doubler.feature.auth.data.remote.dto.RegisterRequestDto
import com.example.doubler.feature.auth.data.remote.dto.RegisterResponseDto
import com.example.doubler.feature.auth.domain.model.User
import com.example.doubler.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(val authApiService: AuthApiService,val preferencesDataSource: PreferencesDataSource): AuthRepository {
    override suspend fun register( name: String,
                                   email: String,
                                   password: String,
                                   deviceName: String,): User {
        val response =  authApiService.register(RegisterRequestDto(
            name,
            email,
            password
        ))
        val userDto = response.user


        val user =  User(
            id = userDto.id,
            name = userDto.name,
            email = userDto.email,
            imageUrl = userDto.imageUrl,
            token=response.token
        )

        preferencesDataSource.saveUser(user)

        return user
    }
    
    override suspend fun login(
        email: String,
        password: String,
        deviceName: String
    ): User {
        val response = authApiService.login(LoginRequestDto(
            email = email,
            password = password,
            device_name = deviceName
        ))
        val userDto = response.user

        val user = User(
            id = userDto.id,
            name = userDto.name,
            email = userDto.email,
            imageUrl = userDto.imageUrl,
            token = response.token
        )

        preferencesDataSource.saveUser(user)

        return user
    }
}