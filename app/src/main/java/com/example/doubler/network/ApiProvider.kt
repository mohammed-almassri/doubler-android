package com.example.notthefinal.core.network

import android.content.Context
import android.util.Log
import com.example.doubler.feature.auth.data.local.PreferencesDataSource
import com.example.doubler.feature.auth.data.remote.api.AuthApiService
import com.example.doubler.feature.email.data.remote.api.EmailApiService
import com.example.doubler.feature.persona.data.remote.api.PersonaApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiProvider private constructor(context: Context) {
    
    private val preferencesDataSource = PreferencesDataSource(context)
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val headerInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithHeaders = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(requestWithHeaders)
    }
    
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        
        // Skip authorization for register and login endpoints
        val skipAuth = url.contains("/register") || url.contains("/login")
        
        if (skipAuth) {
            chain.proceed(originalRequest)
        } else {
            try {
                // Get token from DataStore
                val user = runBlocking { preferencesDataSource.getUser().first() }
                val token = user?.token
                
                if (token != null) {
                    val requestWithAuth = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    Log.d("ApiProvider", "Adding Authorization header for: $url")
                    chain.proceed(requestWithAuth)
                } else {
                    Log.w("ApiProvider", "No token available for: $url")
                    chain.proceed(originalRequest)
                }
            } catch (e: Exception) {
                Log.e("ApiProvider", "Error getting token for authorization", e)
                chain.proceed(originalRequest)
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit: Lazy<Retrofit> = lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.112.1:8000/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val authApiService: AuthApiService by lazy {
        try {
            retrofit.value.create(AuthApiService::class.java)
        } catch (e: Exception) {
            Log.e("ApiProvider", "Failed to init Auth API", e)
            throw e
        }
    }
    
    val emailApiService: EmailApiService by lazy {
        try {
            retrofit.value.create(EmailApiService::class.java)
        } catch (e: Exception) {
            Log.e("ApiProvider", "Failed to init Email API", e)
            throw e
        }
    }
    
    val personaApiService: PersonaApiService by lazy {
        try {
            retrofit.value.create(PersonaApiService::class.java)
        } catch (e: Exception) {
            Log.e("ApiProvider", "Failed to init Persona API", e)
            throw e
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ApiProvider? = null
        
        fun getInstance(context: Context): ApiProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiProvider(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}