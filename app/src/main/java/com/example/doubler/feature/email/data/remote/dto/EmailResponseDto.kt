package com.example.doubler.feature.email.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

data class EmailResponseDto(
    val message: String?,
    val data: EmailDto
)

data class EmailListResponseDto(
    val data: List<EmailDto>,
    val links: PaginationLinksDto,
    val meta: PaginationMetaDto
)

data class EmailDto(
    val id: String,
    @SerializedName("sender_id")
    val senderId: String?,
    @SerializedName("from_email")
    val fromEmail: String?,
    @SerializedName("from_name")
    val fromName: String?,
    @SerializedName("to_emails")
    val toEmails: List<String>?,
    @SerializedName("cc_emails")
    val ccEmails: List<String>? = null,
    @SerializedName("bcc_emails")
    val bccEmails: List<String>? = null,
    val subject: String?,
    val body: String?,
    @SerializedName("body_plain")
    val bodyPlain: String? = null,
    val attachments: List<AttachmentDto>? = null,
    val type: String?,
    val status: String?,
    @SerializedName("sent_at")
    val sentAt: String? = null,
    @SerializedName("message_id")
    val messageId: String? = null,
    @SerializedName("in_reply_to")
    val inReplyTo: String? = null,
    @SerializedName("is_starred")
    val isStarred: Boolean = false,
    @SerializedName("is_spam")
    val isSpam: Boolean = false,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    val recipients: List<EmailRecipientDto>? = null,
    val sender: EmailSenderDto? = null
)

data class EmailRecipientDto(
    val id: String,
    @SerializedName("email_id")
    val emailId: String?,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("email_address")
    val emailAddress: String?,
    val name: String? = null,
    val type: String?,
    @SerializedName("is_delivered")
    val isDelivered: Boolean = false,
    @SerializedName("delivered_at")
    val deliveredAt: String? = null
)

data class EmailSenderDto(
    val id: String,
    val name: String?,
    val email: String
)

data class PaginationLinksDto(
    val first: String?,
    val last: String?,
    val prev: String?,
    val next: String?
)

data class PaginationMetaDto(
    @SerializedName("current_page")
    val currentPage: Int,
    val from: Int?,
    @SerializedName("last_page")
    val lastPage: Int,
    val path: String?,
    @SerializedName("per_page")
    val perPage: Int,
    val to: Int?,
    val total: Int
)