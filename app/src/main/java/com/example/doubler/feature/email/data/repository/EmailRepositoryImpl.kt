package com.example.doubler.feature.email.data.repository

import android.util.Log
import com.example.doubler.core.network.connectivity.NetworkConnectivityObserver
import com.example.doubler.core.network.error.ApiException
import com.example.doubler.feature.email.data.local.datasource.EmailLocalDataSource
import com.example.doubler.feature.email.data.mapper.EmailMapper
import com.example.doubler.feature.email.data.remote.api.EmailApiService
import com.example.doubler.feature.email.data.remote.dto.SendEmailRequestDto
import com.example.doubler.feature.email.domain.model.Attachment
import com.example.doubler.feature.email.domain.model.Email
import com.example.doubler.feature.email.domain.repository.EmailRepository

class EmailRepositoryImpl(
    private val emailApiService: EmailApiService,
    private val localDataSource: EmailLocalDataSource,
    private val networkObserver: NetworkConnectivityObserver
) : EmailRepository {

    override suspend fun sendEmail(
        to: List<String>,
        cc: List<String>?,
        bcc: List<String>?,
        subject: String,
        body: String,
        bodyPlain: String?,
        isDraft: Boolean,
        inReplyTo: String?,
        personaId: String,
        attachments: List<Attachment>?
    ): Email {
        return try {
            Log.d("EmailRepository", "Sending email to: $to, subject: $subject, isDraft: $isDraft")
            
            // If it's a draft or we're offline, save locally first
            if (isDraft || !networkObserver.isNetworkAvailable()) {
                Log.d("EmailRepository", "Saving email locally (draft: $isDraft, offline: ${!networkObserver.isNetworkAvailable()})")
                
                // Create a local email object
                val localEmail = Email(
                    id = generateLocalId(),
                    senderId = null,
                    fromEmail = null,
                    fromName = null,
                    toEmails = to,
                    ccEmails = cc,
                    bccEmails = bcc,
                    subject = subject,
                    body = body,
                    bodyPlain = bodyPlain,
                    attachments = attachments,
                    type = com.example.doubler.feature.email.domain.model.EmailType.OUTGOING,
                    status = if (isDraft) com.example.doubler.feature.email.domain.model.EmailStatus.DRAFT 
                            else com.example.doubler.feature.email.domain.model.EmailStatus.SENT,
                    sentAt = if (!isDraft) java.util.Date() else null,
                    messageId = null,
                    inReplyTo = inReplyTo,
                    isStarred = false,
                    isSpam = false,
                    isRead = true,
                    createdAt = java.util.Date(),
                    updatedAt = java.util.Date(),
                    recipients = null,
                    sender = null
                )
                
                // Save to local database
                localDataSource.insertEmail(localEmail)
                
                // If it's not a draft and we're online, try to send to server
                if (!isDraft && networkObserver.isNetworkAvailable()) {
                    try {
                        val requestDto = SendEmailRequestDto(
                            to = to,
                            cc = cc,
                            bcc = bcc,
                            subject = subject,
                            body = body,
                            bodyPlain = bodyPlain,
                            isDraft = false,
                            inReplyTo = inReplyTo,
                            personaId = personaId,
                            attachments = attachments?.map { EmailMapper.mapToAttachmentDto(it) }
                        )
                        
                        val response = emailApiService.sendEmail(requestDto)
                        val serverEmail = EmailMapper.mapToEmail(response.data)
                        
                        // Update local database with server response
                        localDataSource.updateEmail(serverEmail)
                        Log.d("EmailRepository", "Successfully sent email and updated local cache")
                        return serverEmail
                    } catch (e: Exception) {
                        Log.w("EmailRepository", "Failed to send to server, keeping local copy", e)
                        // Keep the local version, will sync later
                    }
                }
                
                return localEmail
            }
            
            // Online sending for non-drafts
            val requestDto = SendEmailRequestDto(
                to = to,
                cc = cc,
                bcc = bcc,
                subject = subject,
                body = body,
                bodyPlain = bodyPlain,
                isDraft = isDraft,
                inReplyTo = inReplyTo,
                personaId = personaId,
                attachments = attachments?.map { EmailMapper.mapToAttachmentDto(it) }
            )

            val response = emailApiService.sendEmail(requestDto)
            val email = EmailMapper.mapToEmail(response.data)
            
            // Cache the email locally
            localDataSource.insertEmail(email)
            
            Log.d("EmailRepository", "Successfully sent email")
            email
        } catch (e: ApiException) {
            Log.e("EmailRepository", "API exception while sending email", e)
            throw e
        } catch (e: Exception) {
            Log.e("EmailRepository", "Unexpected error while sending email", e)
            throw ApiException.NetworkException("Failed to send email")
        }
    }
    
    private fun generateLocalId(): String {
        return "local_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    override suspend fun getInbox(
        search: String?,
        from: String?,
        status: String?,
        isStarred: Boolean?,
        perPage: Int,
        page: Int
    ): List<Email> {
        return try {
            Log.d("EmailRepository", "Fetching inbox emails (offline-first)")
            
            // Always try to get from local database first
            val localEmails = localDataSource.getInboxEmails(search, from, status, isStarred)
            
            // If we have network, try to sync with server
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing with server")
                    val response = emailApiService.getInbox(search, from, status, isStarred, perPage, page)
                    val serverEmails = response.data.map { EmailMapper.mapToEmail(it) }
                    
                    // Update local cache with server data
                    localDataSource.insertEmails(serverEmails)
                    
                    Log.d("EmailRepository", "Successfully synced ${serverEmails.size} inbox emails from server")
                    return serverEmails
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync with server, using local data", e)
                    // Fall back to local data
                }
            } else {
                Log.d("EmailRepository", "No network, using local data")
            }
            
            Log.d("EmailRepository", "Returning ${localEmails.size} inbox emails from local storage")
            localEmails
            
        } catch (e: Exception) {
            Log.e("EmailRepository", "Error fetching inbox emails", e)
            // As last resort, try local data
            try {
                localDataSource.getInboxEmails(search, from, status, isStarred)
            } catch (localError: Exception) {
                Log.e("EmailRepository", "Failed to get local inbox emails", localError)
                throw ApiException.NetworkException("Failed to fetch inbox")
            }
        }
    }

    override suspend fun getOutbox(
        search: String?,
        status: String?,
        isStarred: Boolean?,
        perPage: Int,
        page: Int
    ): List<Email> {
        return try {
            Log.d("EmailRepository", "Fetching outbox emails (offline-first)")
            
            val localEmails = localDataSource.getOutboxEmails(search, status, isStarred)
            
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing outbox with server")
                    val response = emailApiService.getOutbox(search, status, isStarred, perPage, page)
                    val serverEmails = response.data.map { EmailMapper.mapToEmail(it) }
                    
                    localDataSource.insertEmails(serverEmails)
                    Log.d("EmailRepository", "Successfully synced ${serverEmails.size} outbox emails from server")
                    return serverEmails
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync outbox with server, using local data", e)
                }
            } else {
                Log.d("EmailRepository", "No network, using local outbox data")
            }
            
            Log.d("EmailRepository", "Returning ${localEmails.size} outbox emails from local storage")
            localEmails

        } catch (e: Exception) {
            Log.e("EmailRepository", "Error fetching outbox emails", e)
            try {
                localDataSource.getOutboxEmails(search, status, isStarred)
            } catch (localError: Exception) {
                Log.e("EmailRepository", "Failed to get local outbox emails", localError)
                throw ApiException.NetworkException("Failed to fetch outbox")
            }
        }
    }

    override suspend fun getDrafts(
        search: String?,
        perPage: Int,
        page: Int
    ): List<Email> {
        return try {
            Log.d("EmailRepository", "Fetching draft emails (offline-first)")
            
            val localDrafts = localDataSource.getDraftEmails(search)
            
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing drafts with server")
                    val response = emailApiService.getDrafts(search, perPage, page)
                    val serverDrafts = response.data.map { EmailMapper.mapToEmail(it) }
                    
                    localDataSource.insertEmails(serverDrafts)
                    Log.d("EmailRepository", "Successfully synced ${serverDrafts.size} draft emails from server")
                    return serverDrafts
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync drafts with server, using local data", e)
                }
            } else {
                Log.d("EmailRepository", "No network, using local draft data")
            }
            
            Log.d("EmailRepository", "Returning ${localDrafts.size} draft emails from local storage")
            localDrafts

        } catch (e: Exception) {
            Log.e("EmailRepository", "Error fetching draft emails", e)
            try {
                localDataSource.getDraftEmails(search)
            } catch (localError: Exception) {
                Log.e("EmailRepository", "Failed to get local draft emails", localError)
                throw ApiException.NetworkException("Failed to fetch drafts")
            }
        }
    }

    override suspend fun getStarred(
        search: String?,
        perPage: Int,
        page: Int
    ): List<Email> {
        return try {
            Log.d("EmailRepository", "Fetching starred emails (offline-first)")
            
            val localStarred = localDataSource.getStarredEmails(search)
            
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing starred emails with server")
                    val response = emailApiService.getStarred(search, perPage, page)
                    val serverStarred = response.data.map { EmailMapper.mapToEmail(it) }
                    
                    localDataSource.insertEmails(serverStarred)
                    Log.d("EmailRepository", "Successfully synced ${serverStarred.size} starred emails from server")
                    return serverStarred
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync starred emails with server, using local data", e)
                }
            } else {
                Log.d("EmailRepository", "No network, using local starred data")
            }
            
            Log.d("EmailRepository", "Returning ${localStarred.size} starred emails from local storage")
            localStarred

        } catch (e: Exception) {
            Log.e("EmailRepository", "Error fetching starred emails", e)
            try {
                localDataSource.getStarredEmails(search)
            } catch (localError: Exception) {
                Log.e("EmailRepository", "Failed to get local starred emails", localError)
                throw ApiException.NetworkException("Failed to fetch starred emails")
            }
        }
    }

    override suspend fun getEmail(id: String): Email {
        return try {
            Log.d("EmailRepository", "Fetching email with ID: $id (offline-first)")
            
            // Try local first
            val localEmail = localDataSource.getEmailById(id)
            
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing email with server")
                    val response = emailApiService.getEmail(id)
                    val serverEmail = EmailMapper.mapToEmail(response.data)
                    
                    // Update local cache
                    localDataSource.updateEmail(serverEmail)
                    Log.d("EmailRepository", "Successfully synced email: ${serverEmail.subject}")
                    return serverEmail
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync email with server, using local data", e)
                    if (localEmail != null) {
                        return localEmail
                    } else {
                        throw e
                    }
                }
            } else {
                Log.d("EmailRepository", "No network, using local email data")
                if (localEmail != null) {
                    return localEmail
                } else {
                    throw ApiException.NetworkException("Email not available offline")
                }
            }

        } catch (e: ApiException) {
            Log.e("EmailRepository", "API exception while fetching email $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("EmailRepository", "Unexpected error while fetching email $id", e)
            throw ApiException.NetworkException("Failed to fetch email")
        }
    }

    override suspend fun toggleStar(id: String): Email {
        return try {
            Log.d("EmailRepository", "Toggling star for email $id")
            
            // Update local first (optimistic update)
            val localEmail = localDataSource.toggleStar(id)
                ?: throw ApiException.NotFoundException("Email not found")
            
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing star toggle with server")
                    val response = emailApiService.toggleStar(id)
                    val serverEmail = EmailMapper.mapToEmail(response.data)
                    
                    // Update local cache with server response
                    localDataSource.updateEmail(serverEmail)
                    Log.d("EmailRepository", "Successfully toggled star on server for email $id")
                    return serverEmail
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync star toggle with server", e)
                    // Local change already applied, will sync later
                }
            } else {
                Log.d("EmailRepository", "No network, star toggle saved locally")
            }
            
            return localEmail

        } catch (e: ApiException) {
            Log.e("EmailRepository", "API exception while toggling star for email $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("EmailRepository", "Unexpected error while toggling star for email $id", e)
            throw ApiException.NetworkException("Failed to toggle star")
        }
    }

    override suspend fun toggleSpam(id: String): Email {
        return try {
            Log.d("EmailRepository", "Toggling spam for email $id")
            
            // Update local first (optimistic update)
            val localEmail = localDataSource.toggleSpam(id)
                ?: throw ApiException.NotFoundException("Email not found")
            
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing spam toggle with server")
                    val response = emailApiService.toggleSpam(id)
                    val serverEmail = EmailMapper.mapToEmail(response.data)
                    
                    // Update local cache with server response
                    localDataSource.updateEmail(serverEmail)
                    Log.d("EmailRepository", "Successfully toggled spam on server for email $id")
                    return serverEmail
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync spam toggle with server", e)
                    // Local change already applied, will sync later
                }
            } else {
                Log.d("EmailRepository", "No network, spam toggle saved locally")
            }
            
            return localEmail

        } catch (e: ApiException) {
            Log.e("EmailRepository", "API exception while toggling spam for email $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("EmailRepository", "Unexpected error while toggling spam for email $id", e)
            throw ApiException.NetworkException("Failed to toggle spam")
        }
    }

    override suspend fun deleteEmail(id: String): Email {
        return try {
            Log.d("EmailRepository", "Deleting email $id")
            
            // Delete from local first
            val localEmail = localDataSource.deleteEmail(id)
                ?: throw ApiException.NotFoundException("Email not found")
            
            if (networkObserver.isNetworkAvailable()) {
                try {
                    Log.d("EmailRepository", "Network available, syncing delete with server")
                    val response = emailApiService.deleteEmail(id)
                    val serverEmail = EmailMapper.mapToEmail(response.data)
                    Log.d("EmailRepository", "Successfully deleted email on server $id")
                    return serverEmail
                } catch (e: Exception) {
                    Log.w("EmailRepository", "Failed to sync delete with server", e)
                    // Local delete already applied, will sync later
                }
            } else {
                Log.d("EmailRepository", "No network, email deletion saved locally")
            }
            
            return localEmail

        } catch (e: ApiException) {
            Log.e("EmailRepository", "API exception while deleting email $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("EmailRepository", "Unexpected error while deleting email $id", e)
            throw ApiException.NetworkException("Failed to delete email")
        }
    }
}