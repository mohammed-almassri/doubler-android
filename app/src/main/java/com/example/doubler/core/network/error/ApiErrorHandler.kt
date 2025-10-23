package com.example.doubler.core.network.error

import android.util.Log
import com.google.gson.Gson
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorHandler {
    
    private val gson = Gson()
    
    fun handleException(exception: Throwable): ApiException {
        Log.e("ApiErrorHandler", "Handling exception: ${exception.message}", exception)
        
        return when (exception) {
            is HttpException -> handleHttpException(exception)
            is ConnectException, is UnknownHostException -> {
                ApiException.NetworkException("Network connection failed. Please check your internet connection.")
            }
            is SocketTimeoutException -> {
                ApiException.NetworkException("Request timed out. Please try again.")
            }
            is ApiException -> exception
            else -> {
                ApiException.UnknownException("An unexpected error occurred: ${exception.message}")
            }
        }
    }
    
    private fun handleHttpException(httpException: HttpException): ApiException {
        val errorBody = httpException.response()?.errorBody()?.string()
        Log.d("ApiErrorHandler", "HTTP ${httpException.code()}: $errorBody")
        
        val apiError = try {
            if (errorBody != null) {
                gson.fromJson(errorBody, ApiErrorResponse::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiErrorHandler", "Failed to parse error response", e)
            null
        }
        
        val message = apiError?.message ?: getDefaultErrorMessage(httpException.code())
        val errors = apiError?.errors
        
        return when (httpException.code()) {
            401 -> ApiException.UnauthorizedException(message)
            403 -> ApiException.ForbiddenException(message)
            404 -> ApiException.NotFoundException(message)
            422 -> ApiException.ValidationException(message, errors)
            500 -> ApiException.ServerException(message)
            503 -> ApiException.ServiceUnavailableException(message)
            else -> ApiException.UnknownException("HTTP ${httpException.code()}: $message")
        }
    }
    
    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            401 -> "Authentication required. Please log in again."
            403 -> "Access forbidden. You don't have permission to perform this action."
            404 -> "The requested resource was not found."
            422 -> "Invalid data provided. Please check your input and try again."
            500 -> "Server error occurred. Please try again later."
            503 -> "Service temporarily unavailable. Please try again later."
            else -> "An error occurred. Please try again."
        }
    }
    
    fun getUserFriendlyMessage(exception: ApiException): String {
        return when (exception) {
            is ApiException.UnauthorizedException -> "Your session has expired. Please log in again."
            is ApiException.ForbiddenException -> "You don't have permission to access this content."
            is ApiException.NotFoundException -> "The requested content could not be found."
            is ApiException.ValidationException -> {
                if (exception.errors?.isNotEmpty() == true) {
                    "Validation errors:\n${exception.errors.joinToString("\n• ", "• ")}"
                } else {
                    exception.message
                }
            }
            is ApiException.ServerException -> "Server error. Our team has been notified. Please try again later."
            is ApiException.ServiceUnavailableException -> "Service is temporarily unavailable. Please try again in a few minutes."
            is ApiException.NetworkException -> exception.message
            is ApiException.UnknownException -> "Something went wrong. Please try again."
        }
    }
}