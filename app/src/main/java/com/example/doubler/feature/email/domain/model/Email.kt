package com.example.doubler.feature.email.domain.model

import java.util.Date

data class Email(
    val id: String,
    val senderId: String?,
    val fromEmail: String?,
    val fromName: String?,
    val toEmails: List<String>?,
    val ccEmails: List<String>? = null,
    val bccEmails: List<String>? = null,
    val subject: String?,
    val body: String?,
    val bodyPlain: String? = null,
    val attachments: List<Attachment>? = null,
    val type: EmailType?,
    val status: EmailStatus?,
    val sentAt: Date? = null,
    val messageId: String? = null,
    val inReplyTo: String? = null,
    val isStarred: Boolean = false,
    val isSpam: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Date?,
    val updatedAt: Date?,
    val recipients: List<EmailRecipient>? = null,
    val sender: EmailSender? = null
)

data class Attachment(
    val name: String?,
    val url: String?,
    val size: Long,
    val type: String
)

data class EmailRecipient(
    val id: String,
    val emailId: String?,
    val userId: String? = null,
    val emailAddress: String?,
    val name: String? = null,
    val type: RecipientType?,
    val isDelivered: Boolean = false,
    val deliveredAt: Date? = null
)

data class EmailSender(
    val id: String,
    val name: String?,
    val email: String
)

enum class EmailType {
    INCOMING, OUTGOING
}

enum class EmailStatus {
    DRAFT, SENT, DELIVERED, FAILED, READ
}

enum class RecipientType {
    TO, CC, BCC
}
