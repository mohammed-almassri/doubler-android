package com.example.doubler.core.user.data.repository

import com.example.doubler.core.user.domain.repository.UserRepository
import com.example.doubler.feature.auth.data.local.PreferencesDataSource
import com.example.doubler.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource
) : UserRepository {
    
    override fun getCurrentUser(): Flow<User?> {
        return preferencesDataSource.getUser()
    }
    
    override suspend fun clearUser() {
        preferencesDataSource.clearUser()
    }
}