package com.example.doubler.feature.persona.data.repository

import android.util.Log
import com.example.doubler.core.network.error.ApiErrorHandler
import com.example.doubler.core.network.error.ApiException
import com.example.doubler.feature.persona.data.local.datasource.PersonaLocalDataSource
import com.example.doubler.feature.persona.data.mapper.PersonaMapper
import com.example.doubler.feature.persona.data.remote.api.PersonaApiService
import com.example.doubler.feature.persona.data.remote.dto.*
import com.example.doubler.feature.persona.domain.model.Persona
import com.example.doubler.feature.persona.domain.repository.PersonaRepository

class PersonaRepositoryImpl(
    private val personaApiService: PersonaApiService,
    private val personaLocalDataSource: PersonaLocalDataSource,
) : PersonaRepository {

    override suspend fun getPersonas(
        search: String?,
        perPage: Int,
        page: Int,
        withTrashed: Boolean
    ): List<Persona> {
        return try {
            Log.d("PersonaRepository", "Fetching personas")
            val response = personaApiService.getPersonas(search, perPage, page, withTrashed)
                val personas = PersonaMapper.mapToPersonaList(response.body()!!.data)
                Log.d("PersonaRepository", "Successfully fetched ${personas.size} personas")
                personas
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while fetching personas", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while fetching personas", e)
            throw ApiException.NetworkException("Failed to fetch personas")
        }
    }

    override suspend fun getPersonaById(id: String, withTrashed: Boolean): Persona? {
        return try {
            Log.d("PersonaRepository", "Fetching persona with ID: $id")
            val response = personaApiService.getPersonaById(id, withTrashed)
                val persona = PersonaMapper.mapToPersona(response.body()!!.data)
                Log.d("PersonaRepository", "Successfully fetched persona: ${persona.name}")
                persona
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while fetching persona $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while fetching persona $id", e)
            throw ApiException.NetworkException("Failed to fetch persona")
        }
    }

    override suspend fun createPersona(
        name: String,
        email: String?,
        phone: String?,
        imageUrl: String?,
        bio: String?
    ): Persona {
        return try {
            Log.d("PersonaRepository", "Creating persona: $name")
            val request = CreatePersonaRequestDto(name, email, phone, imageUrl, bio)
            val response = personaApiService.createPersona(request)
                val persona = PersonaMapper.mapToPersona(response.body()!!.data)
                Log.d("PersonaRepository", "Successfully created persona: ${persona.name}")
                personaLocalDataSource.insertPersona(persona = persona)
                persona
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while creating persona", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while creating persona", e)
            throw ApiException.NetworkException("Failed to create persona")
        }
    }

    override suspend fun updatePersona(
        id: String,
        name: String?,
        email: String?,
        phone: String?,
        imageUrl: String?,
        bio: String?
    ): Persona {
        return try {
            Log.d("PersonaRepository", "Updating persona $id")
            val request = UpdatePersonaRequestDto(name, email, phone, imageUrl, bio)
            val response = personaApiService.updatePersona(id, request)
                val persona = PersonaMapper.mapToPersona(response.body()!!.data)
                Log.d("PersonaRepository", "Successfully updated persona $id")
                persona
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while updating persona $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while updating persona $id", e)
            throw ApiException.NetworkException("Failed to update persona")
        }
    }

    override suspend fun deletePersona(id: String): Boolean {
        return try {
            Log.d("PersonaRepository", "Deleting persona $id")
            val response = personaApiService.deletePersona(id)
                Log.d("PersonaRepository", "Successfully deleted persona $id")
                true
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while deleting persona $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while deleting persona $id", e)
            throw ApiException.NetworkException("Failed to delete persona")
        }
    }

    override suspend fun restorePersona(id: String): Persona {
        return try {
            Log.d("PersonaRepository", "Restoring persona $id")
            val response = personaApiService.restorePersona(id)
                val persona = PersonaMapper.mapToPersona(response.body()!!.data)
                Log.d("PersonaRepository", "Successfully restored persona $id")
                persona
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while restoring persona $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while restoring persona $id", e)
            throw ApiException.NetworkException("Failed to restore persona")
        }
    }

    override suspend fun forceDeletePersona(id: String): Boolean {
        return try {
            Log.d("PersonaRepository", "Force deleting persona $id")
            val response = personaApiService.forceDeletePersona(id)
                Log.d("PersonaRepository", "Successfully force deleted persona $id")
                true
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while force deleting persona $id", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while force deleting persona $id", e)
            throw ApiException.NetworkException("Failed to force delete persona")
        }
    }

    override suspend fun getTrashedPersonas(
        search: String?,
        perPage: Int,
        page: Int
    ): List<Persona> {
        return try {
            Log.d("PersonaRepository", "Fetching trashed personas")
            val response = personaApiService.getTrashedPersonas(search, perPage, page)
                val personas = PersonaMapper.mapToPersonaList(response.body()!!.data)
                Log.d("PersonaRepository", "Successfully fetched ${personas.size} trashed personas")
                personas
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while fetching trashed personas", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while fetching trashed personas", e)
            throw ApiException.NetworkException("Failed to fetch trashed personas")
        }
    }

    override suspend fun generateImage(prompt: String): String {
        return try {
            Log.d("PersonaRepository", "Generating image for prompt: $prompt")
            val request = GenerateImageRequestDto(prompt)
            val response = personaApiService.generateImage(request)
                val imageUrl = response.body()!!.data.url
                Log.d("PersonaRepository", "Successfully generated image: $imageUrl")
                imageUrl
        } catch (e: ApiException) {
            Log.e("PersonaRepository", "API exception while generating image", e)
            throw e
        } catch (e: Exception) {
            Log.e("PersonaRepository", "Unexpected error while generating image", e)
            throw ApiException.NetworkException("Failed to generate image")
        }
    }
}