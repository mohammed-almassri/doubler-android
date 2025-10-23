package com.example.doubler.core.user.domain.repository

import com.example.doubler.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun clearUser()
}