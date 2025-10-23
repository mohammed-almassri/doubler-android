package com.example.doubler.core.network.error

import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    val message: String,
    val errors: List<String>? = null
)

// Custom exceptions for different HTTP status codes
sealed class ApiException(message: String) : Exception(message) {
    data class UnauthorizedException(override val message: String) : ApiException(message)
    data class ForbiddenException(override val message: String) : ApiException(message)
    data class NotFoundException(override val message: String) : ApiException(message)
    data class ValidationException(override val message: String, val errors: List<String>? = null) : ApiException(message)
    data class ServerException(override val message: String) : ApiException(message)
    data class ServiceUnavailableException(override val message: String) : ApiException(message)
    data class NetworkException(override val message: String) : ApiException(message)
    data class UnknownException(override val message: String) : ApiException(message)
}