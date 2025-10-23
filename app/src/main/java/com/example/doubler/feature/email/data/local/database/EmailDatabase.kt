package com.example.doubler.feature.email.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.doubler.feature.email.data.local.dao.EmailDao
import com.example.doubler.feature.email.data.local.dao.EmailRecipientDao
import com.example.doubler.feature.email.data.local.dao.EmailSenderDao
import com.example.doubler.feature.email.data.local.entity.EmailEntity
import com.example.doubler.feature.email.data.local.entity.EmailRecipientEntity
import com.example.doubler.feature.email.data.local.entity.EmailSenderEntity
import com.example.doubler.feature.email.data.local.entity.EmailTypeConverters

@Database(
    entities = [
        EmailEntity::class,
        EmailRecipientEntity::class,
        EmailSenderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(EmailTypeConverters::class)
abstract class EmailDatabase : RoomDatabase() {
    
    abstract fun emailDao(): EmailDao
    abstract fun emailRecipientDao(): EmailRecipientDao
    abstract fun emailSenderDao(): EmailSenderDao
    
    companion object {
        @Volatile
        private var INSTANCE: EmailDatabase? = null
        
        fun getDatabase(context: Context): EmailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmailDatabase::class.java,
                    "email_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}