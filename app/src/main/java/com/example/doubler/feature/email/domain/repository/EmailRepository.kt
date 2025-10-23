package com.example.doubler.feature.email.domain.repository

import com.example.doubler.feature.email.domain.model.Attachment
import com.example.doubler.feature.email.domain.model.Email

interface EmailRepository {
    suspend fun sendEmail(
        to: List<String>,
        cc: List<String>? = null,
        bcc: List<String>? = null,
        subject: String,
        body: String,
        bodyPlain: String? = null,
        isDraft: Boolean = false,
        inReplyTo: String? = null,
        attachments: List<Attachment>? = null
    ): Email
    
    suspend fun getInbox(
        search: String? = null,
        from: String? = null,
        status: String? = null,
        isStarred: Boolean? = null,
        perPage: Int = 20,
        page: Int = 1
    ): List<Email>
    
    suspend fun getOutbox(
        search: String? = null,
        status: String? = null,
        isStarred: Boolean? = null,
        perPage: Int = 20,
        page: Int = 1
    ): List<Email>
    
    suspend fun getDrafts(
        search: String? = null,
        perPage: Int = 20,
        page: Int = 1
    ): List<Email>
    
    suspend fun getStarred(
        search: String? = null,
        perPage: Int = 20,
        page: Int = 1
    ): List<Email>
    
    suspend fun getEmail(id: String): Email
    
    suspend fun toggleStar(id: String): Email
    
    suspend fun toggleSpam(id: String): Email
    
    suspend fun deleteEmail(id: String): Email
}