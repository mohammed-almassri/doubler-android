package com.example.doubler.feature.persona.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String?,
    val email: String?,
    val bio: String?,
)