package com.example.doubler.feature.persona.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.doubler.feature.persona.data.local.entity.PersonaEntity

@Dao
interface PersonaDao {
    @Query("select * from personas limit 1")
    suspend fun findPersona(): PersonaEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity)
    @Update
    suspend fun updatePersona(persona: PersonaEntity)
    @Query("DELETE FROM personas")
    suspend fun clearAllPersonas()
}