package com.example.doubler.feature.email.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

@Entity(tableName = "emails")
@TypeConverters(EmailTypeConverters::class)
data class EmailEntity(
    @PrimaryKey
    val id: String,
    val senderId: String?,
    val fromEmail: String?,
    val fromName: String?,
    val toEmails: List<String>?,
    val ccEmails: List<String>?,
    val bccEmails: List<String>?,
    val subject: String?,
    val body: String?,
    val bodyPlain: String?,
    val attachments: List<AttachmentEntity>?,
    val type: String?, // EmailType as String
    val status: String?, // EmailStatus as String
    val sentAt: Date?,
    val messageId: String?,
    val inReplyTo: String?,
    val isStarred: Boolean = false,
    val isSpam: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Date?,
    val updatedAt: Date?,
    val lastSyncAt: Date = Date(), // For sync tracking
    val isLocalOnly: Boolean = false // For drafts not yet synced
)

@Entity(tableName = "email_recipients")
data class EmailRecipientEntity(
    @PrimaryKey
    val id: String,
    val emailId: String,
    val userId: String?,
    val emailAddress: String?,
    val name: String?,
    val type: String?, // RecipientType as String
    val isDelivered: Boolean = false,
    val deliveredAt: Date?
)

@Entity(tableName = "email_senders")
data class EmailSenderEntity(
    @PrimaryKey
    val id: String,
    val name: String?,
    val email: String
)

data class AttachmentEntity(
    val name: String?,
    val url: String?,
    val size: Long,
    val type: String
)

class EmailTypeConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType)
        }
    }
    
    @TypeConverter
    fun fromAttachmentList(value: List<AttachmentEntity>?): String? {
        return if (value == null) null else gson.toJson(value)
    }
    
    @TypeConverter
    fun toAttachmentList(value: String?): List<AttachmentEntity>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<AttachmentEntity>>() {}.type
            gson.fromJson(value, listType)
        }
    }
    
    @TypeConverter
    fun fromDate(value: Date?): Long? {
        return value?.time
    }
    
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }
}