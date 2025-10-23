package com.example.doubler.feature.email.data.local.datasource

import com.example.doubler.feature.email.data.local.dao.EmailDao
import com.example.doubler.feature.email.data.local.dao.EmailRecipientDao
import com.example.doubler.feature.email.data.local.dao.EmailSenderDao
import com.example.doubler.feature.email.data.local.mapper.EmailEntityMapper
import com.example.doubler.feature.email.domain.model.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmailLocalDataSource(
    private val emailDao: EmailDao,
    private val recipientDao: EmailRecipientDao,
    private val senderDao: EmailSenderDao
) {
    
    suspend fun getEmailById(id: String): Email? {
        val emailEntity = emailDao.getEmailById(id)
        return emailEntity?.let { EmailEntityMapper.toDomain(it) }
    }
    
    suspend fun getInboxEmails(
        search: String? = null,
        from: String? = null,
        status: String? = null,
        isStarred: Boolean? = null
    ): List<Email> {
        val emails = when {
            search != null -> emailDao.searchInboxEmails(search)
            from != null -> emailDao.getInboxEmailsFromSender(from)
            status != null -> emailDao.getInboxEmailsByStatus(status)
            isStarred == true -> emailDao.getInboxStarredEmails()
            else -> emailDao.getInboxEmails()
        }
        return emails.map { EmailEntityMapper.toDomain(it) }
    }
    
    suspend fun getOutboxEmails(
        search: String? = null,
        status: String? = null,
        isStarred: Boolean? = null
    ): List<Email> {
        val emails = when {
            search != null -> emailDao.searchOutboxEmails(search)
            status != null -> emailDao.getOutboxEmailsByStatus(status)
            isStarred == true -> emailDao.getOutboxStarredEmails()
            else -> emailDao.getOutboxEmails()
        }
        return emails.map { EmailEntityMapper.toDomain(it) }
    }
    
    suspend fun getDraftEmails(search: String? = null): List<Email> {
        val emails = if (search != null) {
            emailDao.searchDraftEmails(search)
        } else {
            emailDao.getDraftEmails()
        }
        return emails.map { EmailEntityMapper.toDomain(it) }
    }
    
    suspend fun getStarredEmails(search: String? = null): List<Email> {
        val emails = if (search != null) {
            emailDao.searchStarredEmails(search)
        } else {
            emailDao.getStarredEmails()
        }
        return emails.map { EmailEntityMapper.toDomain(it) }
    }
    
    suspend fun insertEmail(email: Email) {
        val emailEntity = EmailEntityMapper.toEntity(email)
        emailDao.insertEmail(emailEntity)
        
        // Insert recipients if present
        email.recipients?.forEach { recipient ->
            val recipientEntity = EmailEntityMapper.toRecipientEntity(recipient)
            recipientDao.insertRecipient(recipientEntity)
        }
        
        // Insert sender if present
        email.sender?.let { sender ->
            val senderEntity = EmailEntityMapper.toSenderEntity(sender)
            senderDao.insertSender(senderEntity)
        }
    }
    
    suspend fun insertEmails(emails: List<Email>) {
        val emailEntities = emails.map { EmailEntityMapper.toEntity(it) }
        emailDao.insertEmails(emailEntities)
        
        // Insert all recipients and senders
        emails.forEach { email ->
            email.recipients?.forEach { recipient ->
                val recipientEntity = EmailEntityMapper.toRecipientEntity(recipient)
                recipientDao.insertRecipient(recipientEntity)
            }
            
            email.sender?.let { sender ->
                val senderEntity = EmailEntityMapper.toSenderEntity(sender)
                senderDao.insertSender(senderEntity)
            }
        }
    }
    
    suspend fun updateEmail(email: Email) {
        val emailEntity = EmailEntityMapper.toEntity(email)
        emailDao.updateEmail(emailEntity)
    }
    
    suspend fun toggleStar(id: String): Email? {
        emailDao.toggleStar(id)
        return getEmailById(id)
    }
    
    suspend fun toggleSpam(id: String): Email? {
        emailDao.toggleSpam(id)
        return getEmailById(id)
    }
    
    suspend fun deleteEmail(id: String): Email? {
        val email = getEmailById(id)
        emailDao.deleteEmailById(id)
        recipientDao.deleteRecipientsByEmailId(id)
        return email
    }
    
    suspend fun markAsRead(id: String, isRead: Boolean = true) {
        emailDao.markAsRead(id, isRead)
    }
    
    suspend fun clearAllEmails() {
        emailDao.deleteAllEmails()
    }
    
    suspend fun deleteOldEmails(cutoffDate: java.util.Date) {
        emailDao.deleteOldEmails(cutoffDate)
    }
    
    // Flow methods for reactive UI
    fun getInboxEmailsFlow(): Flow<List<Email>> {
        return emailDao.getInboxEmailsFlow().map { entities ->
            entities.map { EmailEntityMapper.toDomain(it) }
        }
    }
    
    fun getOutboxEmailsFlow(): Flow<List<Email>> {
        return emailDao.getOutboxEmailsFlow().map { entities ->
            entities.map { EmailEntityMapper.toDomain(it) }
        }
    }
    
    fun getDraftEmailsFlow(): Flow<List<Email>> {
        return emailDao.getDraftEmailsFlow().map { entities ->
            entities.map { EmailEntityMapper.toDomain(it) }
        }
    }
    
    fun getStarredEmailsFlow(): Flow<List<Email>> {
        return emailDao.getStarredEmailsFlow().map { entities ->
            entities.map { EmailEntityMapper.toDomain(it) }
        }
    }
}