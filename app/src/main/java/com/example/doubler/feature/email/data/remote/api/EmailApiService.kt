package com.example.doubler.feature.email.data.remote.api

import com.example.doubler.feature.email.data.remote.dto.EmailListResponseDto
import com.example.doubler.feature.email.data.remote.dto.EmailResponseDto
import com.example.doubler.feature.email.data.remote.dto.SendEmailRequestDto
import retrofit2.http.*

interface EmailApiService {
    
    @POST("emails/send")
    suspend fun sendEmail(@Body requestDto: SendEmailRequestDto): EmailResponseDto
    
    @GET("emails/inbox")
    suspend fun getInbox(
        @Query("search") search: String? = null,
        @Query("from") from: String? = null,
        @Query("status") status: String? = null,
        @Query("is_starred") isStarred: Boolean? = null,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): EmailListResponseDto
    
    @GET("emails/outbox")
    suspend fun getOutbox(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("is_starred") isStarred: Boolean? = null,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): EmailListResponseDto
    
    @GET("emails/drafts")
    suspend fun getDrafts(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): EmailListResponseDto
    
    @GET("emails/starred")
    suspend fun getStarred(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): EmailListResponseDto
    
    @GET("emails/{id}")
    suspend fun getEmail(@Path("id") id: String): EmailResponseDto
    
    @POST("emails/{id}/star")
    suspend fun toggleStar(@Path("id") id: String): EmailResponseDto
    
    @POST("emails/{id}/spam")
    suspend fun toggleSpam(@Path("id") id: String): EmailResponseDto
    
    @DELETE("emails/{id}")
    suspend fun deleteEmail(@Path("id") id: String): EmailResponseDto
}