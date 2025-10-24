package com.example.doubler.core.auth.domain.repository

interface LogoutRepository {
    suspend fun logout()
}