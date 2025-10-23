package com.example.doubler.feature.email.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SendEmailRequestDto(
    val to: List<String>,
    val cc: List<String>? = null,
    val bcc: List<String>? = null,
    val subject: String,
    val body: String,
    @SerializedName("body_plain")
    val bodyPlain: String? = null,
    @SerializedName("is_draft")
    val isDraft: Boolean = false,
    @SerializedName("in_reply_to")
    val inReplyTo: String? = null,
    @SerializedName("persona_id")
    val personaId: String,
    val attachments: List<AttachmentDto>? = null
)

data class AttachmentDto(
    val name: String?,
    val url: String?,
    val size: Long,
    val type: String
)