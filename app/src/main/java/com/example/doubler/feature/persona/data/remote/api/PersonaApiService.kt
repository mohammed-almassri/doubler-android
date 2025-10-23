package com.example.doubler.feature.persona.data.remote.api

import com.example.doubler.feature.persona.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PersonaApiService {
    
    @GET("personas")
    suspend fun getPersonas(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1,
        @Query("with_trashed") withTrashed: Boolean = false
    ): Response<PersonaListResponseDto>
    
    @GET("personas/{id}")
    suspend fun getPersonaById(
        @Path("id") id: String,
        @Query("with_trashed") withTrashed: Boolean = false
    ): Response<PersonaDetailResponseDto>
    
    @POST("personas")
    suspend fun createPersona(
        @Body request: CreatePersonaRequestDto
    ): Response<PersonaDetailResponseDto>
    
    @PUT("personas/{id}")
    suspend fun updatePersona(
        @Path("id") id: String,
        @Body request: UpdatePersonaRequestDto
    ): Response<PersonaDetailResponseDto>
    
    @DELETE("personas/{id}")
    suspend fun deletePersona(
        @Path("id") id: String
    ): Response<Unit>
    
    @POST("personas/{id}/restore")
    suspend fun restorePersona(
        @Path("id") id: String
    ): Response<PersonaDetailResponseDto>
    
    @DELETE("personas/{id}/force")
    suspend fun forceDeletePersona(
        @Path("id") id: String
    ): Response<Unit>
    
    @GET("personas/trashed")
    suspend fun getTrashedPersonas(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<PersonaListResponseDto>
    
    @POST("personas/images/generate")
    suspend fun generateImage(
        @Body request: GenerateImageRequestDto
    ): Response<GenerateImageResponseDto>
}