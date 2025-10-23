package com.example.doubler.feature.persona.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.doubler.feature.persona.data.local.dao.PersonaDao
import com.example.doubler.feature.persona.data.local.entity.PersonaEntity

@Database(
    entities = [
        PersonaEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class PersonaDatabase : RoomDatabase() {
    
    abstract fun personaDao(): PersonaDao

    companion object {
        @Volatile
        private var INSTANCE: PersonaDatabase? = null
        
        fun getDatabase(context: Context): PersonaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PersonaDatabase::class.java,
                    "persona_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}