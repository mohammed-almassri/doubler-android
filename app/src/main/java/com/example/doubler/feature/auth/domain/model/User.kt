package com.example.doubler.feature.auth.domain.model

data class User(
    val id: String,
    val name:String,
    val email:String,
    val imageUrl:String?,
    val token: String,
)