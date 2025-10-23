package com.example.doubler.feature.email.data.local.mapper

import com.example.doubler.feature.email.data.local.entity.AttachmentEntity
import com.example.doubler.feature.email.data.local.entity.EmailEntity
import com.example.doubler.feature.email.data.local.entity.EmailRecipientEntity
import com.example.doubler.feature.email.data.local.entity.EmailSenderEntity
import com.example.doubler.feature.email.domain.model.*
import java.util.Date

object EmailEntityMapper {
    
    fun toEntity(email: Email): EmailEntity {
        return EmailEntity(
            id = email.id,
            senderId = email.senderId,
            fromEmail = email.fromEmail,
            fromName = email.fromName,
            toEmails = email.toEmails,
            ccEmails = email.ccEmails,
            bccEmails = email.bccEmails,
            subject = email.subject,
            body = email.body,
            bodyPlain = email.bodyPlain,
            attachments = email.attachments?.map { toAttachmentEntity(it) },
            type = email.type?.name,
            status = email.status?.name,
            sentAt = email.sentAt,
            messageId = email.messageId,
            inReplyTo = email.inReplyTo,
            isStarred = email.isStarred,
            isSpam = email.isSpam,
            isRead = email.isRead,
            createdAt = email.createdAt,
            updatedAt = email.updatedAt,
            lastSyncAt = Date(),
            isLocalOnly = false
        )
    }
    
    fun toDomain(entity: EmailEntity): Email {
        return Email(
            id = entity.id,
            senderId = entity.senderId,
            fromEmail = entity.fromEmail,
            fromName = entity.fromName,
            toEmails = entity.toEmails,
            ccEmails = entity.ccEmails,
            bccEmails = entity.bccEmails,
            subject = entity.subject,
            body = entity.body,
            bodyPlain = entity.bodyPlain,
            attachments = entity.attachments?.map { toAttachmentDomain(it) },
            type = entity.type?.let { EmailType.valueOf(it) },
            status = entity.status?.let { EmailStatus.valueOf(it) },
            sentAt = entity.sentAt,
            messageId = entity.messageId,
            inReplyTo = entity.inReplyTo,
            isStarred = entity.isStarred,
            isSpam = entity.isSpam,
            isRead = entity.isRead,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            recipients = null, // Will be populated separately if needed
            sender = null // Will be populated separately if needed
        )
    }
    
    fun toRecipientEntity(recipient: EmailRecipient): EmailRecipientEntity {
        return EmailRecipientEntity(
            id = recipient.id,
            emailId = recipient.emailId ?: "",
            userId = recipient.userId,
            emailAddress = recipient.emailAddress,
            name = recipient.name,
            type = recipient.type?.name,
            isDelivered = recipient.isDelivered,
            deliveredAt = recipient.deliveredAt
        )
    }
    
    fun toRecipientDomain(entity: EmailRecipientEntity): EmailRecipient {
        return EmailRecipient(
            id = entity.id,
            emailId = entity.emailId,
            userId = entity.userId,
            emailAddress = entity.emailAddress,
            name = entity.name,
            type = entity.type?.let { RecipientType.valueOf(it) },
            isDelivered = entity.isDelivered,
            deliveredAt = entity.deliveredAt
        )
    }
    
    fun toSenderEntity(sender: EmailSender): EmailSenderEntity {
        return EmailSenderEntity(
            id = sender.id,
            name = sender.name,
            email = sender.email
        )
    }
    
    fun toSenderDomain(entity: EmailSenderEntity): EmailSender {
        return EmailSender(
            id = entity.id,
            name = entity.name,
            email = entity.email
        )
    }
    
    private fun toAttachmentEntity(attachment: Attachment): AttachmentEntity {
        return AttachmentEntity(
            name = attachment.name,
            url = attachment.url,
            size = attachment.size,
            type = attachment.type
        )
    }
    
    private fun toAttachmentDomain(entity: AttachmentEntity): Attachment {
        return Attachment(
            name = entity.name,
            url = entity.url,
            size = entity.size,
            type = entity.type
        )
    }
}