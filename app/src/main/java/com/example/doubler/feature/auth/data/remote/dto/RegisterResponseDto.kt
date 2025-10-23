package com.example.doubler.feature.auth.data.remote.dto

data class UserDto(
    val name:String,
    val email:String,
    val updatedAt:String,
    val createdAt:String,
    val imageUrl:String?,
    val id:String,
)

data class RegisterResponseDto (
    val message: String,
    val user: UserDto,
    val token: String,
)