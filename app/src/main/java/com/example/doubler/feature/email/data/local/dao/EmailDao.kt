package com.example.doubler.feature.email.data.local.dao

import androidx.room.*
import com.example.doubler.feature.email.data.local.entity.EmailEntity
import com.example.doubler.feature.email.data.local.entity.EmailRecipientEntity
import com.example.doubler.feature.email.data.local.entity.EmailSenderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {
    
    // Email operations
    @Query("SELECT * FROM emails WHERE id = :id")
    suspend fun getEmailById(id: String): EmailEntity?
    
    @Query("SELECT * FROM emails WHERE type = 'INCOMING' ORDER BY createdAt DESC")
    suspend fun getInboxEmails(): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'INCOMING' AND isStarred = 1 ORDER BY createdAt DESC")
    suspend fun getInboxStarredEmails(): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'INCOMING' AND fromEmail LIKE '%' || :from || '%' ORDER BY createdAt DESC")
    suspend fun getInboxEmailsFromSender(from: String): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'INCOMING' AND status = :status ORDER BY createdAt DESC")
    suspend fun getInboxEmailsByStatus(status: String): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'INCOMING' AND (subject LIKE '%' || :search || '%' OR body LIKE '%' || :search || '%') ORDER BY createdAt DESC")
    suspend fun searchInboxEmails(search: String): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'OUTGOING' ORDER BY createdAt DESC")
    suspend fun getOutboxEmails(): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'OUTGOING' AND isStarred = 1 ORDER BY createdAt DESC")
    suspend fun getOutboxStarredEmails(): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'OUTGOING' AND status = :status ORDER BY createdAt DESC")
    suspend fun getOutboxEmailsByStatus(status: String): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE type = 'OUTGOING' AND (subject LIKE '%' || :search || '%' OR body LIKE '%' || :search || '%') ORDER BY createdAt DESC")
    suspend fun searchOutboxEmails(search: String): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE status = 'DRAFT' ORDER BY updatedAt DESC")
    suspend fun getDraftEmails(): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE status = 'DRAFT' AND (subject LIKE '%' || :search || '%' OR body LIKE '%' || :search || '%') ORDER BY updatedAt DESC")
    suspend fun searchDraftEmails(search: String): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE isStarred = 1 ORDER BY createdAt DESC")
    suspend fun getStarredEmails(): List<EmailEntity>
    
    @Query("SELECT * FROM emails WHERE isStarred = 1 AND (subject LIKE '%' || :search || '%' OR body LIKE '%' || :search || '%') ORDER BY createdAt DESC")
    suspend fun searchStarredEmails(search: String): List<EmailEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)
    
    @Update
    suspend fun updateEmail(email: EmailEntity)
    
    @Query("UPDATE emails SET isStarred = NOT isStarred WHERE id = :id")
    suspend fun toggleStar(id: String)
    
    @Query("UPDATE emails SET isSpam = NOT isSpam WHERE id = :id")
    suspend fun toggleSpam(id: String)
    
    @Query("UPDATE emails SET isRead = :isRead WHERE id = :id")
    suspend fun markAsRead(id: String, isRead: Boolean = true)
    
    @Delete
    suspend fun deleteEmail(email: EmailEntity)
    
    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: String)
    
    @Query("DELETE FROM emails")
    suspend fun deleteAllEmails()
    
    @Query("DELETE FROM emails WHERE lastSyncAt < :cutoffDate")
    suspend fun deleteOldEmails(cutoffDate: java.util.Date)
    
    // Flow operations for reactive UI
    @Query("SELECT * FROM emails WHERE type = 'INCOMING' ORDER BY createdAt DESC")
    fun getInboxEmailsFlow(): Flow<List<EmailEntity>>
    
    @Query("SELECT * FROM emails WHERE type = 'OUTGOING' ORDER BY createdAt DESC")
    fun getOutboxEmailsFlow(): Flow<List<EmailEntity>>
    
    @Query("SELECT * FROM emails WHERE status = 'DRAFT' ORDER BY updatedAt DESC")
    fun getDraftEmailsFlow(): Flow<List<EmailEntity>>
    
    @Query("SELECT * FROM emails WHERE isStarred = 1 ORDER BY createdAt DESC")
    fun getStarredEmailsFlow(): Flow<List<EmailEntity>>
}

@Dao
interface EmailRecipientDao {
    
    @Query("SELECT * FROM email_recipients WHERE emailId = :emailId")
    suspend fun getRecipientsByEmailId(emailId: String): List<EmailRecipientEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipient(recipient: EmailRecipientEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipients(recipients: List<EmailRecipientEntity>)
    
    @Delete
    suspend fun deleteRecipient(recipient: EmailRecipientEntity)
    
    @Query("DELETE FROM email_recipients WHERE emailId = :emailId")
    suspend fun deleteRecipientsByEmailId(emailId: String)
    
    @Query("DELETE FROM email_recipients")
    suspend fun clearAllRecipients()
}

@Dao
interface EmailSenderDao {
    
    @Query("SELECT * FROM email_senders WHERE id = :id")
    suspend fun getSenderById(id: String): EmailSenderEntity?
    
    @Query("SELECT * FROM email_senders WHERE email = :email")
    suspend fun getSenderByEmail(email: String): EmailSenderEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSender(sender: EmailSenderEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSenders(senders: List<EmailSenderEntity>)
    
    @Delete
    suspend fun deleteSender(sender: EmailSenderEntity)
    
    @Query("DELETE FROM email_senders")
    suspend fun clearAllSenders()
}