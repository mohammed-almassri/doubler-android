package com.example.doubler.feature.email.data.mapper

import com.example.doubler.feature.email.data.remote.dto.*
import com.example.doubler.feature.email.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

object EmailMapper {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    
    fun mapToEmail(dto: EmailDto): Email {
        return Email(
            id = dto.id,
            senderId = dto.senderId,
            fromEmail = dto.fromEmail,
            fromName = dto.fromName,
            toEmails = dto.toEmails,
            ccEmails = dto.ccEmails,
            bccEmails = dto.bccEmails,
            subject = dto.subject,
            body = dto.body,
            bodyPlain = dto.bodyPlain,
            attachments = dto.attachments?.map { mapToAttachment(it) },
            type = if(dto.type!=null) mapToEmailType(dto.type) else null,
            status = if(dto.status!=null)mapToEmailStatus(dto.status)else null,
            sentAt = dto.sentAt?.let { parseDate(it) },
            messageId = dto.messageId,
            inReplyTo = dto.inReplyTo,
            isStarred = dto.isStarred,
            isSpam = dto.isSpam,
            isRead = dto.isRead,
            createdAt = if(dto.createdAt!=null)parseDate(dto.createdAt) else null,
            updatedAt = if(dto.updatedAt!=null)parseDate(dto.updatedAt) else null,
            recipients = dto.recipients?.map { mapToEmailRecipient(it) },
            sender = dto.sender?.let { mapToEmailSender(it) }
        )
    }
    
    fun mapToAttachment(dto: AttachmentDto): Attachment {
        return Attachment(
            name = dto.name,
            url = dto.url,
            size = dto.size,
            type = dto.type
        )
    }
    
    fun mapToEmailRecipient(dto: EmailRecipientDto): EmailRecipient {
        return EmailRecipient(
            id = dto.id,
            emailId = dto.emailId,
            userId = dto.userId,
            emailAddress = dto.emailAddress,
            name = dto.name,
            type = if(dto.type!=null)mapToRecipientType(dto.type)else null,
            isDelivered = dto.isDelivered,
            deliveredAt = dto.deliveredAt?.let { parseDate(it) }
        )
    }
    
    fun mapToEmailSender(dto: EmailSenderDto): EmailSender {
        return EmailSender(
            id = dto.id,
            name = dto.name,
            email = dto.email
        )
    }
    
    private fun mapToEmailType(type: String): EmailType {
        return when (type.lowercase()) {
            "incoming" -> EmailType.INCOMING
            "outgoing" -> EmailType.OUTGOING
            else -> EmailType.INCOMING
        }
    }
    
    private fun mapToEmailStatus(status: String): EmailStatus {
        return when (status.lowercase()) {
            "draft" -> EmailStatus.DRAFT
            "sent" -> EmailStatus.SENT
            "delivered" -> EmailStatus.DELIVERED
            "failed" -> EmailStatus.FAILED
            "read" -> EmailStatus.READ
            else -> EmailStatus.SENT
        }
    }
    
    private fun mapToRecipientType(type: String): RecipientType {
        return when (type.lowercase()) {
            "to" -> RecipientType.TO
            "cc" -> RecipientType.CC
            "bcc" -> RecipientType.BCC
            else -> RecipientType.TO
        }
    }
    
    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
    
    fun mapToAttachmentDto(attachment: Attachment): AttachmentDto {
        return AttachmentDto(
            name = attachment.name,
            url = attachment.url,
            size = attachment.size,
            type = attachment.type
        )
    }
}